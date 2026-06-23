import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

export interface Cliente {
  id: string;
  nome: string;
  tipo: 'PESSOA_FISICA' | 'PESSOA_JURIDICA';
  cpfCnpj: string;
  email?: string;
  telefone?: string;
  ativo: boolean;
}

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="animate-fade-in" style="display: flex; flex-direction: column; gap: 1.5rem;">
      
      <!-- Top Action Bar -->
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <p style="color: var(--text-secondary); font-size: 0.95rem;">
          Gerencie os dados cadastrais dos clientes do escritório.
        </p>
        <button (click)="openAddModal()" class="btn btn-primary">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
          Novo Cliente
        </button>
      </div>

      <!-- Clients List Card -->
      <div class="card" style="padding: 0;">
        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>Nome</th>
                <th>Tipo</th>
                <th>CPF / CNPJ</th>
                <th>E-mail</th>
                <th>Telefone</th>
                <th>Status</th>
                <th style="text-align: right;">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let cliente of getVisibleClientes()">
                <td style="font-weight: 500; color: var(--text-primary);">{{ cliente.nome }}</td>
                <td>
                  <span class="badge" [ngClass]="cliente.tipo === 'PESSOA_FISICA' ? 'badge-warning' : 'badge-success'" style="font-size: 0.7rem;">
                    {{ cliente.tipo === 'PESSOA_FISICA' ? 'Pessoa Física' : 'Pessoa Jurídica' }}
                  </span>
                </td>
                <td style="font-family: monospace;">{{ formatCpfCnpj(cliente.cpfCnpj) }}</td>
                <td>{{ cliente.email || '-' }}</td>
                <td>{{ cliente.telefone || '-' }}</td>
                <td>
                  <span class="badge" [ngClass]="cliente.ativo ? 'badge-success' : 'badge-danger'">
                    {{ cliente.ativo ? 'Ativo' : 'Inativo' }}
                  </span>
                </td>
                <td style="text-align: right;">
                  <div style="display: inline-flex; gap: 0.5rem; justify-content: flex-end;">
                    <button (click)="openEditModal(cliente)" class="btn btn-secondary" style="padding: 0.4rem 0.75rem; font-size: 0.8rem;">
                      Editar
                    </button>
                    <button (click)="toggleAtivo(cliente)" class="btn" [ngClass]="cliente.ativo ? 'btn-logout' : 'btn-primary'" style="padding: 0.4rem 0.75rem; font-size: 0.8rem; border-color: transparent;">
                      {{ cliente.ativo ? 'Desativar' : 'Reativar' }}
                    </button>
                  </div>
                </td>
              </tr>
              <tr *ngIf="getVisibleClientes().length === 0">
                <td colspan="7" style="text-align: center; color: var(--text-muted); padding: 3rem 1.25rem;">
                   Nenhum cliente cadastrado.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Add/Edit Modal -->
      <div class="modal-backdrop" *ngIf="showModal">
        <div class="modal-content animate-fade-in">
          <div class="modal-header">
            <h3>{{ isEditMode ? 'Editar Cliente' : 'Novo Cliente' }}</h3>
            <button (click)="closeModal()" class="modal-close">&times;</button>
          </div>
          <form (ngSubmit)="saveCliente()" #clienteForm="ngForm">
            <div class="modal-body">

              <!-- Error Alert -->
              <div *ngIf="errorMessage" class="alert alert-danger" style="margin-bottom: 1rem; padding: 0.75rem 1rem; border-radius: 0.5rem; font-size: 0.9rem;">
                {{ errorMessage }}
              </div>

              <!-- Name input -->
              <div class="form-group">
                <label class="form-label" for="nome">Nome / Razão Social</label>
                <input
                  type="text"
                  id="nome"
                  name="nome"
                  class="form-input"
                  placeholder="Nome completo ou Razão Social"
                  [(ngModel)]="formData.nome"
                  required
                  #nomeInput="ngModel"
                />
                <span *ngIf="nomeInput.touched && nomeInput.invalid" class="form-error">
                  O nome é obrigatório.
                </span>
              </div>

              <!-- Tipo input -->
              <div class="form-group">
                <label class="form-label">Tipo de Cliente</label>
                <div style="display: flex; gap: 1.5rem; margin-top: 0.25rem;">
                  <label class="checkbox-label" style="display: inline-flex; align-items: center; gap: 0.4rem;">
                    <input
                      type="radio"
                      name="tipo"
                      [(ngModel)]="formData.tipo"
                      value="PESSOA_FISICA"
                      style="accent-color: var(--accent-color);"
                    />
                    Pessoa Física (PF)
                  </label>
                  <label class="checkbox-label" style="display: inline-flex; align-items: center; gap: 0.4rem;">
                    <input
                      type="radio"
                      name="tipo"
                      [(ngModel)]="formData.tipo"
                      value="PESSOA_JURIDICA"
                      style="accent-color: var(--accent-color);"
                    />
                    Pessoa Jurídica (PJ)
                  </label>
                </div>
              </div>

              <!-- CPF / CNPJ input -->
              <div class="form-group">
                <label class="form-label" for="cpfCnpj">
                  {{ formData.tipo === 'PESSOA_FISICA' ? 'CPF' : 'CNPJ' }}
                </label>
                <input
                  type="text"
                  id="cpfCnpj"
                  name="cpfCnpj"
                  class="form-input"
                  [placeholder]="formData.tipo === 'PESSOA_FISICA' ? 'Ex: 111.222.333-44' : 'Ex: 12.345.678/0001-90'"
                  [(ngModel)]="formData.cpfCnpj"
                  required
                  #cpfCnpjInput="ngModel"
                  pattern="^[0-9.-/\\s]{11,18}$"
                />
                <span *ngIf="cpfCnpjInput.touched && cpfCnpjInput.invalid" class="form-error">
                  Insira um documento válido.
                </span>
              </div>

              <!-- Email input -->
              <div class="form-group">
                <label class="form-label" for="email">E-mail (Opcional)</label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  class="form-input"
                  placeholder="cliente@email.com"
                  [(ngModel)]="formData.email"
                  #emailInput="ngModel"
                  email
                />
                <span *ngIf="emailInput.touched && emailInput.invalid" class="form-error">
                  Insira um e-mail válido.
                </span>
              </div>

              <!-- Telefone input -->
              <div class="form-group">
                <label class="form-label" for="telefone">Telefone (Opcional)</label>
                <input
                  type="text"
                  id="telefone"
                  name="telefone"
                  class="form-input"
                  placeholder="(11) 99999-9999"
                  [(ngModel)]="formData.telefone"
                />
              </div>
            </div>
            
            <div class="modal-footer">
              <button type="button" (click)="closeModal()" class="btn btn-secondary">Cancelar</button>
              <button type="submit" [disabled]="clienteForm.invalid || saving" class="btn btn-primary">
                {{ saving ? 'Salvando...' : 'Salvar' }}
              </button>
            </div>
          </form>
        </div>
      </div>

    </div>
  `
})
export class ClientesComponent implements OnInit {
  clientes: Cliente[] = [];
  showModal = false;
  isEditMode = false;
  saving = false;
  errorMessage = '';
  
  formData: Partial<Cliente> = {
    nome: '',
    tipo: 'PESSOA_FISICA',
    cpfCnpj: '',
    email: '',
    telefone: '',
    ativo: true
  };

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadClientes();
  }

  loadClientes() {
    this.http.get<Cliente[]>('/api/clientes').subscribe({
      next: (data) => {
        this.clientes = data;
      },
      error: (err) => {
        console.error('Erro ao carregar clientes:', err);
      }
    });
  }

  getVisibleClientes(): Cliente[] {
    return this.clientes;
  }

  formatCpfCnpj(value: string): string {
    const clean = value.replace(/\D/g, '');
    if (clean.length === 11) {
      return `${clean.substring(0, 3)}.${clean.substring(3, 6)}.${clean.substring(6, 9)}-${clean.substring(9)}`;
    }
    if (clean.length === 14) {
      return `${clean.substring(0, 2)}.${clean.substring(2, 5)}.${clean.substring(5, 8)}/${clean.substring(8, 12)}-${clean.substring(12)}`;
    }
    return value;
  }

  openAddModal() {
    this.isEditMode = false;
    this.errorMessage = '';
    this.formData = {
      nome: '',
      tipo: 'PESSOA_FISICA',
      cpfCnpj: '',
      email: '',
      telefone: '',
      ativo: true
    };
    this.showModal = true;
  }

  openEditModal(cliente: Cliente) {
    this.isEditMode = true;
    this.errorMessage = '';
    this.formData = { ...cliente };
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.errorMessage = '';
    this.saving = false;
  }

  saveCliente() {
    this.saving = true;
    this.errorMessage = '';
    const sanitizedDoc = this.formData.cpfCnpj?.replace(/\D/g, '') || '';
    
    const clientPayload = {
      ...this.formData,
      cpfCnpj: sanitizedDoc
    };

    if (this.isEditMode) {
      this.http.put<Cliente>(`/api/clientes/${this.formData.id}`, clientPayload).subscribe({
        next: (updated) => {
          const idx = this.clientes.findIndex(c => c.id === updated.id);
          if (idx !== -1) {
            this.clientes[idx] = updated;
          }
          this.closeModal();
        },
        error: (err) => {
          this.saving = false;
          this.errorMessage = err.error?.error || 'Erro ao salvar cliente. Tente novamente.';
        }
      });
    } else {
      this.http.post<Cliente>('/api/clientes', clientPayload).subscribe({
        next: (created) => {
          this.clientes.push(created);
          this.closeModal();
        },
        error: (err) => {
          this.saving = false;
          this.errorMessage = err.error?.error || 'Erro ao salvar cliente. Tente novamente.';
        }
      });
    }
  }

  toggleAtivo(cliente: Cliente) {
    const updatedClient = {
      ...cliente,
      ativo: !cliente.ativo
    };
    this.http.put<Cliente>(`/api/clientes/${cliente.id}`, updatedClient).subscribe({
      next: (saved) => {
        const idx = this.clientes.findIndex(c => c.id === saved.id);
        if (idx !== -1) {
          this.clientes[idx] = saved;
        }
      },
      error: (err) => console.error('Erro ao alterar status do cliente:', err)
    });
  }
}
