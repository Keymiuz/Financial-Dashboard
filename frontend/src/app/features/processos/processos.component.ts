import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

export interface Cliente {
  id: string;
  nome: string;
  ativo: boolean;
}

export interface Processo {
  id: string;
  clienteId: string;
  clienteNome?: string;
  numeroCnj: string;
  descricao: string;
  area: 'CIVIL' | 'TRABALHISTA' | 'CRIMINAL' | 'TRIBUTARIO' | 'PREVIDENCIARIO' | 'OUTROS';
  status: 'ATIVO' | 'ENCERRADO' | 'SUSPENSO';
  dataInicio: string;
}

@Component({
  selector: 'app-processos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="animate-fade-in" style="display: flex; flex-direction: column; gap: 1.5rem;">
      
      <!-- Top Action Bar -->
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <p style="color: var(--text-secondary); font-size: 0.95rem;">
          Gerencie as pastas de processos judiciais vinculados aos clientes.
        </p>
        <button (click)="openAddModal()" class="btn btn-primary">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
          Novo Processo
        </button>
      </div>

      <!-- Process List Card -->
      <div class="card" style="padding: 0;">
        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>Número CNJ</th>
                <th>Cliente</th>
                <th>Área</th>
                <th>Descrição</th>
                <th>Data de Início</th>
                <th>Status</th>
                <th style="text-align: right;">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let processo of processos">
                <td style="font-family: monospace; font-weight: 600; color: var(--text-primary);">
                  {{ formatCnj(processo.numeroCnj) }}
                </td>
                <td>{{ processo.clienteNome || 'Desconhecido' }}</td>
                <td>
                  <span class="badge badge-warning" style="background-color: rgba(230, 85, 62, 0.05); color: var(--accent-color); border: 1px solid rgba(230, 85, 62, 0.15);">
                    {{ formatArea(processo.area) }}
                  </span>
                </td>
                <td [title]="processo.descricao" style="max-width: 260px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                  {{ processo.descricao }}
                </td>
                <td>{{ formatDateShort(processo.dataInicio) }}</td>
                <td>
                  <span class="badge" [ngClass]="getStatusClass(processo.status)">
                    {{ formatStatus(processo.status) }}
                  </span>
                </td>
                <td style="text-align: right;">
                  <div style="display: inline-flex; gap: 0.5rem; justify-content: flex-end;">
                    <button (click)="openEditModal(processo)" class="btn btn-secondary" style="padding: 0.4rem 0.75rem; font-size: 0.8rem;">
                      Editar
                    </button>
                    <button (click)="deleteProcesso(processo)" class="btn btn-logout" style="padding: 0.4rem 0.75rem; font-size: 0.8rem; border-color: transparent;">
                      Excluir
                    </button>
                  </div>
                </td>
              </tr>
              <tr *ngIf="processos.length === 0">
                <td colspan="7" style="text-align: center; color: var(--text-muted); padding: 3rem 1.25rem;">
                  Nenhum processo cadastrado.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Add/Edit Modal -->
      <div class="modal-backdrop" *ngIf="showModal">
        <div class="modal-content animate-fade-in" style="max-width: 580px;">
          <div class="modal-header">
            <h3>{{ isEditMode ? 'Editar Processo' : 'Novo Processo' }}</h3>
            <button (click)="closeModal()" class="modal-close">&times;</button>
          </div>
          <form (ngSubmit)="saveProcesso()" #processForm="ngForm">
            <div class="modal-body">

              <!-- Error Alert -->
              <div *ngIf="errorMessage" class="alert alert-danger" style="margin-bottom: 1rem; padding: 0.75rem 1rem; border-radius: 0.5rem; font-size: 0.9rem;">
                {{ errorMessage }}
              </div>
              <!-- Client Selector -->
              <div class="form-group">
                <label class="form-label" for="clienteId">Cliente Associado</label>
                <select
                  id="clienteId"
                  name="clienteId"
                  class="form-input"
                  [(ngModel)]="formData.clienteId"
                  required
                  #clienteSelect="ngModel"
                  style="background-color: hsl(224, 25%, 8%); cursor: pointer;"
                >
                  <option value="" disabled selected>Selecione um cliente...</option>
                  <option *ngFor="let c of activeClientes" [value]="c.id">
                    {{ c.nome }}
                  </option>
                </select>
                <span *ngIf="clienteSelect.touched && clienteSelect.invalid" class="form-error">
                  O cliente é obrigatório.
                </span>
                <span *ngIf="activeClientes.length === 0" class="form-error" style="color: var(--warning);">
                  Nenhum cliente ativo cadastrado. Cadastre um cliente primeiro.
                </span>
              </div>

              <!-- CNJ Number input -->
              <div class="form-group">
                <label class="form-label" for="numeroCnj">Número CNJ</label>
                <input
                  type="text"
                  id="numeroCnj"
                  name="numeroCnj"
                  class="form-input"
                  placeholder="Ex: 5001234-56.2024.8.26.0100"
                  [(ngModel)]="formData.numeroCnj"
                  required
                  #cnjInput="ngModel"
                  pattern="^[0-9.-]{15,25}$"
                />
                <span *ngIf="cnjInput.touched && cnjInput.invalid" class="form-error">
                  O número CNJ é obrigatório e deve ter formato válido.
                </span>
              </div>

              <!-- Area input -->
              <div class="form-group">
                <label class="form-label" for="area">Área do Direito</label>
                <select
                  id="area"
                  name="area"
                  class="form-input"
                  [(ngModel)]="formData.area"
                  required
                  style="background-color: hsl(224, 25%, 8%); cursor: pointer;"
                >
                  <option value="CIVIL">Civil</option>
                  <option value="TRABALHISTA">Trabalhista</option>
                  <option value="CRIMINAL">Criminal</option>
                  <option value="TRIBUTARIO">Tributário</option>
                  <option value="PREVIDENCIARIO">Previdenciário</option>
                  <option value="OUTROS">Outros</option>
                </select>
              </div>

              <!-- Status input -->
              <div class="form-group">
                <label class="form-label" for="status">Status do Processo</label>
                <select
                  id="status"
                  name="status"
                  class="form-input"
                  [(ngModel)]="formData.status"
                  required
                  style="background-color: hsl(224, 25%, 8%); cursor: pointer;"
                >
                  <option value="ATIVO">Ativo</option>
                  <option value="SUSPENSO">Suspenso</option>
                  <option value="ENCERRADO">Encerrado</option>
                </select>
              </div>

              <!-- Start Date input -->
              <div class="form-group">
                <label class="form-label" for="dataInicio">Data de Início</label>
                <input
                  type="date"
                  id="dataInicio"
                  name="dataInicio"
                  class="form-input"
                  [(ngModel)]="formData.dataInicio"
                  required
                  #dateInput="ngModel"
                  style="cursor: pointer;"
                />
                <span *ngIf="dateInput.touched && dateInput.invalid" class="form-error">
                  A data de início é obrigatória.
                </span>
              </div>

              <!-- Description input -->
              <div class="form-group">
                <label class="form-label" for="descricao">Descrição do Processo</label>
                <textarea
                  id="descricao"
                  name="descricao"
                  class="form-input"
                  placeholder="Detalhes ou objeto do processo..."
                  rows="3"
                  [(ngModel)]="formData.descricao"
                  required
                  #descInput="ngModel"
                  style="resize: vertical; font-family: inherit;"
                ></textarea>
                <span *ngIf="descInput.touched && descInput.invalid" class="form-error">
                  A descrição é obrigatória.
                </span>
              </div>

            </div>
            
            <div class="modal-footer">
              <button type="button" (click)="closeModal()" class="btn btn-secondary">Cancelar</button>
              <button type="submit" [disabled]="processForm.invalid || saving" class="btn btn-primary">
                {{ saving ? 'Salvando...' : 'Salvar' }}
              </button>
            </div>
          </form>
        </div>
      </div>

    </div>
  `
})
export class ProcessosComponent implements OnInit {
  processos: Processo[] = [];
  activeClientes: Cliente[] = [];
  
  showModal = false;
  isEditMode = false;
  saving = false;
  errorMessage = '';
  
  formData: Partial<Processo> = {
    clienteId: '',
    numeroCnj: '',
    descricao: '',
    area: 'CIVIL',
    status: 'ATIVO',
    dataInicio: ''
  };

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadClientes();
    this.loadProcessos();
  }

  loadClientes() {
    this.http.get<any[]>('/api/clientes').subscribe({
      next: (data) => {
        this.activeClientes = data.filter(c => c.ativo);
      },
      error: (err) => console.error('Erro ao carregar clientes:', err)
    });
  }

  loadProcessos() {
    this.http.get<Processo[]>('/api/processos').subscribe({
      next: (data) => {
        this.processos = data;
      },
      error: (err) => console.error('Erro ao carregar processos:', err)
    });
  }

  formatCnj(cnj: string): string {
    const clean = cnj.replace(/\D/g, '');
    if (clean.length === 20) {
      return `${clean.substring(0, 7)}-${clean.substring(7, 9)}.${clean.substring(9, 13)}.${clean.substring(13, 14)}.${clean.substring(14, 16)}.${clean.substring(16)}`;
    }
    return cnj;
  }

  formatArea(area: string): string {
    const map: any = {
      CIVIL: 'Civil',
      TRABALHISTA: 'Trabalhista',
      CRIMINAL: 'Criminal',
      TRIBUTARIO: 'Tributário',
      PREVIDENCIARIO: 'Previdenciário',
      OUTROS: 'Outros'
    };
    return map[area] || area;
  }

  formatStatus(status: string): string {
    const map: any = {
      ATIVO: 'Ativo',
      SUSPENSO: 'Suspenso',
      ENCERRADO: 'Encerrado'
    };
    return map[status] || status;
  }

  getStatusClass(status: string): string {
    if (status === 'ATIVO') return 'badge-success';
    if (status === 'SUSPENSO') return 'badge-warning';
    return 'badge-danger';
  }

  formatDateShort(dateStr: string): string {
    const parts = dateStr.split('-');
    if (parts.length < 3) return dateStr;
    return `${parts[2]}/${parts[1]}/${parts[0]}`;
  }

  openAddModal() {
    this.loadClientes(); // Reload fresh active clients
    this.isEditMode = false;
    this.errorMessage = '';
    this.formData = {
      clienteId: this.activeClientes.length > 0 ? this.activeClientes[0].id : '',
      numeroCnj: '',
      descricao: '',
      area: 'CIVIL',
      status: 'ATIVO',
      dataInicio: new Date().toISOString().split('T')[0]
    };
    this.showModal = true;
  }

  openEditModal(processo: Processo) {
    this.loadClientes();
    this.isEditMode = true;
    this.errorMessage = '';
    this.formData = { ...processo };
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.errorMessage = '';
    this.saving = false;
  }

  saveProcesso() {
    this.saving = true;
    this.errorMessage = '';
    const cleanCnj = this.formData.numeroCnj?.replace(/\D/g, '') || '';
    
    const processoPayload = {
      ...this.formData,
      numeroCnj: cleanCnj
    };

    if (this.isEditMode) {
      this.http.put<Processo>(`/api/processos/${this.formData.id}`, processoPayload).subscribe({
        next: (updated) => {
          const idx = this.processos.findIndex(p => p.id === updated.id);
          if (idx !== -1) {
            this.processos[idx] = updated;
          }
          this.closeModal();
        },
        error: (err) => {
          this.saving = false;
          this.errorMessage = err.error?.error || 'Erro ao salvar processo. Tente novamente.';
        }
      });
    } else {
      this.http.post<Processo>('/api/processos', processoPayload).subscribe({
        next: (created) => {
          this.processos.push(created);
          this.closeModal();
        },
        error: (err) => {
          this.saving = false;
          this.errorMessage = err.error?.error || 'Erro ao salvar processo. Tente novamente.';
        }
      });
    }
  }

  deleteProcesso(processo: Processo) {
    if (confirm(`Deseja realmente excluir o processo CNJ ${this.formatCnj(processo.numeroCnj)}?`)) {
      this.http.delete(`/api/processos/${processo.id}`).subscribe({
        next: () => {
          this.processos = this.processos.filter(p => p.id !== processo.id);
        },
        error: (err) => console.error('Erro ao excluir processo:', err)
      });
    }
  }
}
