import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

interface Contrato {
  id: string;
  processoId: string;
  processoNumeroCnj: string;
  clienteNome: string;
  tipo: 'FIXO' | 'HORA';
  valorFixo?: number;
  valorHora?: number;
  descricao: string;
  dataContrato: string;
  totalContratado: number;
  totalRecebido: number;
  totalPendente: number;
  // UI state
  showParcelas?: boolean;
  parcelas?: Parcela[];
  loadingParcelas?: boolean;
}

interface Parcela {
  id: string;
  contratoId: string;
  numeroParcela: number;
  valor: number;
  dataVencimento: string;
  status: 'PENDENTE' | 'RECEBIDO' | 'ATRASADO';
  dataRecebimento?: string;
  observacao?: string;
}

interface Processo {
  id: string;
  numeroCnj: string;
  clienteNome: string;
}

@Component({
  selector: 'app-honorarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="animate-fade-in" style="display: flex; flex-direction: column; gap: 1.5rem;">

      <!-- Top bar -->
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <p style="color: var(--text-secondary); font-size: 0.95rem;">
          Gerencie contratos de honorários e acompanhe o recebimento de cada parcela.
        </p>
        <button (click)="openAddContrato()" class="btn btn-primary">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
          Novo Contrato
        </button>
      </div>

      <!-- Empty state -->
      <div *ngIf="contratos.length === 0 && !loading" class="card" style="text-align: center; padding: 3rem;">
        <p style="color: var(--text-muted); font-size: 1rem;">Nenhum contrato de honorários cadastrado.</p>
        <p style="color: var(--text-muted); font-size: 0.85rem; margin-top: 0.5rem;">Crie um novo contrato vinculado a um processo para começar a registrar honorários.</p>
      </div>

      <!-- Contracts List -->
      <div *ngFor="let contrato of contratos" class="card" style="padding: 0; overflow: hidden;">
        
        <!-- Contract Header -->
        <div 
          (click)="toggleParcelas(contrato)"
          style="display: flex; justify-content: space-between; align-items: flex-start; padding: 1.25rem 1.5rem; cursor: pointer; gap: 1rem;"
          [style.background]="contrato.showParcelas ? 'hsl(224, 20%, 11%)' : ''"
        >
          <!-- Left side: info -->
          <div style="display: flex; flex-direction: column; gap: 0.3rem; flex: 1; min-width: 0;">
            <div style="display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
              <span style="font-weight: 600; color: var(--text-primary); font-size: 0.95rem;">{{ contrato.clienteNome }}</span>
              <span class="badge badge-warning" style="font-size: 0.7rem; font-family: monospace;">{{ formatCnj(contrato.processoNumeroCnj) }}</span>
              <span class="badge" [ngClass]="contrato.tipo === 'FIXO' ? 'badge-success' : 'badge-warning'" style="font-size: 0.7rem;">
                {{ contrato.tipo === 'FIXO' ? 'Fixo' : 'Por Hora' }}
              </span>
            </div>
            <span style="font-size: 0.85rem; color: var(--text-secondary);">{{ contrato.descricao }}</span>
            <span style="font-size: 0.78rem; color: var(--text-muted);">Contrato em: {{ formatDate(contrato.dataContrato) }}</span>
          </div>

          <!-- Right side: financials -->
          <div style="display: flex; gap: 1.5rem; align-items: center; flex-shrink: 0;">
            <div style="text-align: right;">
              <div style="font-size: 0.7rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em;">Total</div>
              <div style="font-size: 0.95rem; font-weight: 600; color: var(--text-primary);">R$ {{ formatMoney(contrato.totalContratado) }}</div>
            </div>
            <div style="text-align: right;">
              <div style="font-size: 0.7rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em;">Recebido</div>
              <div style="font-size: 0.95rem; font-weight: 600; color: var(--success);">R$ {{ formatMoney(contrato.totalRecebido) }}</div>
            </div>
            <div style="text-align: right;">
              <div style="font-size: 0.7rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em;">Pendente</div>
              <div style="font-size: 0.95rem; font-weight: 600;" [style.color]="contrato.totalPendente > 0 ? 'var(--warning)' : 'var(--text-muted)'">
                R$ {{ formatMoney(contrato.totalPendente) }}
              </div>
            </div>
            <!-- Chevron -->
            <svg [style.transform]="contrato.showParcelas ? 'rotate(180deg)' : ''" style="transition: transform 0.2s; flex-shrink: 0; color: var(--text-muted);" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"></polyline></svg>
          </div>
        </div>

        <!-- Parcelas Panel -->
        <div *ngIf="contrato.showParcelas" style="border-top: 1px solid var(--border-color);">
          
          <!-- Loading -->
          <div *ngIf="contrato.loadingParcelas" style="padding: 2rem; text-align: center; color: var(--text-muted);">
            Carregando parcelas...
          </div>

          <!-- Parcelas Table -->
          <div *ngIf="!contrato.loadingParcelas">
            <table class="data-table" style="margin: 0;">
              <thead>
                <tr>
                  <th style="width: 48px;">Nº</th>
                  <th>Valor</th>
                  <th>Vencimento</th>
                  <th>Recebimento</th>
                  <th>Status</th>
                  <th>Obs.</th>
                  <th style="text-align: right;">Ações</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let p of contrato.parcelas">
                  <td style="color: var(--text-muted); font-size: 0.85rem;">{{ p.numeroParcela }}</td>
                  <td style="font-weight: 600;">R$ {{ formatMoney(p.valor) }}</td>
                  <td>{{ formatDate(p.dataVencimento) }}</td>
                  <td>{{ p.dataRecebimento ? formatDate(p.dataRecebimento) : '—' }}</td>
                  <td>
                    <span class="badge" [ngClass]="getStatusClass(p.status)" style="font-size: 0.72rem;">
                      {{ formatStatus(p.status) }}
                    </span>
                  </td>
                  <td style="font-size: 0.82rem; color: var(--text-secondary); max-width: 160px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" [title]="p.observacao || ''">
                    {{ p.observacao || '—' }}
                  </td>
                  <td style="text-align: right;">
                    <div style="display: inline-flex; gap: 0.4rem;">
                      <button 
                        *ngIf="p.status !== 'RECEBIDO'"
                        (click)="openReceberModal(p, contrato)"
                        class="btn btn-primary"
                        style="padding: 0.3rem 0.65rem; font-size: 0.78rem; gap: 0.25rem;"
                      >
                        ✓ Recebido
                      </button>
                      <button 
                        *ngIf="p.status === 'RECEBIDO'"
                        (click)="desfazerRecebimento(p, contrato)"
                        class="btn btn-secondary"
                        style="padding: 0.3rem 0.65rem; font-size: 0.78rem;"
                      >
                        Desfazer
                      </button>
                    </div>
                  </td>
                </tr>
                <tr *ngIf="!contrato.parcelas || contrato.parcelas.length === 0">
                  <td colspan="7" style="text-align: center; color: var(--text-muted); padding: 2rem;">
                    Nenhuma parcela cadastrada para este contrato.
                  </td>
                </tr>
              </tbody>
            </table>

            <!-- Add Parcela button -->
            <div style="padding: 1rem 1.5rem; border-top: 1px solid var(--border-color);">
              <button (click)="openAddParcela(contrato)" class="btn btn-secondary" style="font-size: 0.85rem; padding: 0.45rem 1rem;">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
                Adicionar Parcela
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- ─── Modal: Novo Contrato ─── -->
      <div class="modal-backdrop" *ngIf="showContratoModal">
        <div class="modal-content animate-fade-in" style="max-width: 520px;">
          <div class="modal-header">
            <h3>Novo Contrato de Honorários</h3>
            <button (click)="closeContratoModal()" class="modal-close">&times;</button>
          </div>
          <form (ngSubmit)="saveContrato()" #contratoForm="ngForm">
            <div class="modal-body">

              <div *ngIf="contratoError" class="alert alert-danger" style="margin-bottom: 1rem; padding: 0.75rem 1rem; border-radius: 0.5rem; font-size: 0.9rem;">
                {{ contratoError }}
              </div>

              <div class="form-group">
                <label class="form-label" for="processoId">Processo</label>
                <select id="processoId" name="processoId" class="form-input" [(ngModel)]="contratoForm_.processoId" required style="background-color: hsl(224, 25%, 8%); cursor: pointer;">
                  <option value="" disabled>Selecione um processo...</option>
                  <option *ngFor="let p of processos" [value]="p.id">{{ formatCnj(p.numeroCnj) }} — {{ p.clienteNome }}</option>
                </select>
              </div>

              <div class="form-group">
                <label class="form-label">Tipo de Contrato</label>
                <div style="display: flex; gap: 1.5rem; margin-top: 0.25rem;">
                  <label style="display: inline-flex; align-items: center; gap: 0.4rem;">
                    <input type="radio" name="tipo" [(ngModel)]="contratoForm_.tipo" value="FIXO" style="accent-color: var(--accent-color);" /> Valor Fixo
                  </label>
                  <label style="display: inline-flex; align-items: center; gap: 0.4rem;">
                    <input type="radio" name="tipo" [(ngModel)]="contratoForm_.tipo" value="HORA" style="accent-color: var(--accent-color);" /> Por Hora
                  </label>
                </div>
              </div>

              <div class="form-group" *ngIf="contratoForm_.tipo === 'FIXO'">
                <label class="form-label" for="valorFixo">Valor Fixo Total (R$)</label>
                <input type="number" id="valorFixo" name="valorFixo" class="form-input" placeholder="Ex: 5000.00" step="0.01" min="0" [(ngModel)]="contratoForm_.valorFixo" />
              </div>

              <div class="form-group" *ngIf="contratoForm_.tipo === 'HORA'">
                <label class="form-label" for="valorHora">Valor por Hora (R$)</label>
                <input type="number" id="valorHora" name="valorHora" class="form-input" placeholder="Ex: 350.00" step="0.01" min="0" [(ngModel)]="contratoForm_.valorHora" />
              </div>

              <div class="form-group">
                <label class="form-label" for="contratoDescricao">Descrição</label>
                <input type="text" id="contratoDescricao" name="contratoDescricao" class="form-input" placeholder="Ex: Contrato de honorários — Ação Trabalhista" [(ngModel)]="contratoForm_.descricao" required />
              </div>

              <div class="form-group">
                <label class="form-label" for="dataContrato">Data do Contrato</label>
                <input type="date" id="dataContrato" name="dataContrato" class="form-input" [(ngModel)]="contratoForm_.dataContrato" required style="cursor: pointer;" />
              </div>

            </div>
            <div class="modal-footer">
              <button type="button" (click)="closeContratoModal()" class="btn btn-secondary">Cancelar</button>
              <button type="submit" [disabled]="contratoForm.invalid || savingContrato" class="btn btn-primary">
                {{ savingContrato ? 'Salvando...' : 'Salvar' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- ─── Modal: Nova Parcela ─── -->
      <div class="modal-backdrop" *ngIf="showParcelaModal">
        <div class="modal-content animate-fade-in" style="max-width: 460px;">
          <div class="modal-header">
            <h3>Adicionar Parcela</h3>
            <button (click)="closeParcelaModal()" class="modal-close">&times;</button>
          </div>
          <form (ngSubmit)="saveParcela()" #parcelaFormRef="ngForm">
            <div class="modal-body">

              <div *ngIf="parcelaError" class="alert alert-danger" style="margin-bottom: 1rem; padding: 0.75rem 1rem; border-radius: 0.5rem; font-size: 0.9rem;">
                {{ parcelaError }}
              </div>

              <div class="form-group">
                <label class="form-label" for="valorParcela">Valor da Parcela (R$)</label>
                <input type="number" id="valorParcela" name="valorParcela" class="form-input" placeholder="Ex: 1250.00" step="0.01" min="0.01" [(ngModel)]="parcelaForm_.valor" required />
              </div>

              <div class="form-group">
                <label class="form-label" for="dataVencimento">Data de Vencimento</label>
                <input type="date" id="dataVencimento" name="dataVencimento" class="form-input" [(ngModel)]="parcelaForm_.dataVencimento" required style="cursor: pointer;" />
              </div>

              <div class="form-group">
                <label class="form-label" for="observacao">Observação (Opcional)</label>
                <input type="text" id="observacao" name="observacao" class="form-input" placeholder="Ex: 1ª parcela do acordo" [(ngModel)]="parcelaForm_.observacao" />
              </div>

            </div>
            <div class="modal-footer">
              <button type="button" (click)="closeParcelaModal()" class="btn btn-secondary">Cancelar</button>
              <button type="submit" [disabled]="parcelaFormRef.invalid || savingParcela" class="btn btn-primary">
                {{ savingParcela ? 'Salvando...' : 'Adicionar' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- ─── Modal: Marcar Recebido ─── -->
      <div class="modal-backdrop" *ngIf="showReceberModal">
        <div class="modal-content animate-fade-in" style="max-width: 400px;">
          <div class="modal-header">
            <h3>Confirmar Recebimento</h3>
            <button (click)="closeReceberModal()" class="modal-close">&times;</button>
          </div>
          <div class="modal-body">
            <p style="color: var(--text-secondary); margin-bottom: 1.25rem;">
              Parcela de <strong style="color: var(--text-primary);">R$ {{ receberForm_.parcelaValor | number:'1.2-2' }}</strong>
            </p>
            <div class="form-group">
              <label class="form-label" for="dataRecebimento">Data de Recebimento</label>
              <input type="date" id="dataRecebimento" name="dataRecebimento" class="form-input" [(ngModel)]="receberForm_.dataRecebimento" style="cursor: pointer;" />
            </div>
          </div>
          <div class="modal-footer">
            <button (click)="closeReceberModal()" class="btn btn-secondary">Cancelar</button>
            <button (click)="confirmarRecebimento()" [disabled]="savingReceber" class="btn btn-primary" style="background-color: var(--success); border-color: var(--success);">
              {{ savingReceber ? 'Salvando...' : '✓ Confirmar Recebimento' }}
            </button>
          </div>
        </div>
      </div>

    </div>
  `
})
export class HonorariosComponent implements OnInit {
  contratos: Contrato[] = [];
  processos: Processo[] = [];
  loading = true;

  // Contrato modal
  showContratoModal = false;
  savingContrato = false;
  contratoError = '';
  contratoForm_: any = { processoId: '', tipo: 'FIXO', valorFixo: null, valorHora: null, descricao: '', dataContrato: '' };

  // Parcela modal
  showParcelaModal = false;
  savingParcela = false;
  parcelaError = '';
  selectedContratoForParcela: Contrato | null = null;
  parcelaForm_: any = { valor: null, dataVencimento: '', observacao: '' };

  // Receber modal
  showReceberModal = false;
  savingReceber = false;
  selectedParcela: Parcela | null = null;
  selectedContratoForReceber: Contrato | null = null;
  receberForm_: any = { dataRecebimento: '', parcelaValor: 0 };

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadContratos();
    this.loadProcessos();
  }

  loadContratos() {
    this.loading = true;
    this.http.get<Contrato[]>('/api/honorarios/contratos').subscribe({
      next: (data) => {
        this.contratos = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar contratos:', err);
        this.loading = false;
      }
    });
  }

  loadProcessos() {
    this.http.get<any[]>('/api/processos').subscribe({
      next: (data) => {
        this.processos = data.map(p => ({ id: p.id, numeroCnj: p.numeroCnj, clienteNome: p.clienteNome }));
      },
      error: (err) => console.error('Erro ao carregar processos:', err)
    });
  }

  toggleParcelas(contrato: Contrato) {
    contrato.showParcelas = !contrato.showParcelas;
    if (contrato.showParcelas && !contrato.parcelas) {
      this.loadParcelas(contrato);
    }
  }

  loadParcelas(contrato: Contrato) {
    contrato.loadingParcelas = true;
    this.http.get<Parcela[]>(`/api/honorarios/contratos/${contrato.id}/parcelas`).subscribe({
      next: (parcelas) => {
        contrato.parcelas = parcelas;
        contrato.loadingParcelas = false;
      },
      error: (err) => {
        console.error('Erro ao carregar parcelas:', err);
        contrato.loadingParcelas = false;
      }
    });
  }

  // ─── Contrato Modal ─────────────────────────────────────────────────────

  openAddContrato() {
    this.contratoError = '';
    this.contratoForm_ = {
      processoId: this.processos.length > 0 ? this.processos[0].id : '',
      tipo: 'FIXO',
      valorFixo: null,
      valorHora: null,
      descricao: '',
      dataContrato: new Date().toISOString().split('T')[0]
    };
    this.showContratoModal = true;
  }

  closeContratoModal() {
    this.showContratoModal = false;
    this.contratoError = '';
    this.savingContrato = false;
  }

  saveContrato() {
    this.savingContrato = true;
    this.contratoError = '';
    this.http.post<Contrato>('/api/honorarios/contratos', this.contratoForm_).subscribe({
      next: (created) => {
        this.contratos.unshift(created);
        this.closeContratoModal();
      },
      error: (err) => {
        this.savingContrato = false;
        this.contratoError = err.error?.error || 'Erro ao salvar contrato.';
      }
    });
  }

  // ─── Parcela Modal ──────────────────────────────────────────────────────

  openAddParcela(contrato: Contrato) {
    this.selectedContratoForParcela = contrato;
    this.parcelaError = '';
    this.parcelaForm_ = {
      contratoId: contrato.id,
      valor: null,
      dataVencimento: new Date().toISOString().split('T')[0],
      observacao: ''
    };
    this.showParcelaModal = true;
  }

  closeParcelaModal() {
    this.showParcelaModal = false;
    this.parcelaError = '';
    this.savingParcela = false;
  }

  saveParcela() {
    this.savingParcela = true;
    this.parcelaError = '';
    this.http.post<Parcela>('/api/honorarios/parcelas', this.parcelaForm_).subscribe({
      next: (created) => {
        if (this.selectedContratoForParcela) {
          if (!this.selectedContratoForParcela.parcelas) {
            this.selectedContratoForParcela.parcelas = [];
          }
          this.selectedContratoForParcela.parcelas.push(created);
          this.selectedContratoForParcela.totalContratado += created.valor;
          this.selectedContratoForParcela.totalPendente += created.valor;
        }
        this.closeParcelaModal();
      },
      error: (err) => {
        this.savingParcela = false;
        this.parcelaError = err.error?.error || 'Erro ao adicionar parcela.';
      }
    });
  }

  // ─── Receber Modal ──────────────────────────────────────────────────────

  openReceberModal(parcela: Parcela, contrato: Contrato) {
    this.selectedParcela = parcela;
    this.selectedContratoForReceber = contrato;
    this.receberForm_ = {
      dataRecebimento: new Date().toISOString().split('T')[0],
      parcelaValor: parcela.valor
    };
    this.savingReceber = false;
    this.showReceberModal = true;
  }

  closeReceberModal() {
    this.showReceberModal = false;
    this.savingReceber = false;
  }

  confirmarRecebimento() {
    if (!this.selectedParcela) return;
    this.savingReceber = true;
    this.http.patch<Parcela>(
      `/api/honorarios/parcelas/${this.selectedParcela.id}/receber`,
      { dataRecebimento: this.receberForm_.dataRecebimento }
    ).subscribe({
      next: (updated) => {
        if (this.selectedContratoForReceber && this.selectedContratoForReceber.parcelas) {
          const idx = this.selectedContratoForReceber.parcelas.findIndex(p => p.id === updated.id);
          if (idx !== -1) {
            const prev = this.selectedContratoForReceber.parcelas[idx];
            if (prev.status !== 'RECEBIDO') {
              this.selectedContratoForReceber.totalRecebido += prev.valor;
              this.selectedContratoForReceber.totalPendente -= prev.valor;
            }
            this.selectedContratoForReceber.parcelas[idx] = updated;
          }
        }
        this.closeReceberModal();
      },
      error: (err) => {
        this.savingReceber = false;
        console.error('Erro ao marcar recebido:', err);
      }
    });
  }

  desfazerRecebimento(parcela: Parcela, contrato: Contrato) {
    this.http.patch<Parcela>(`/api/honorarios/parcelas/${parcela.id}/pendente`, {}).subscribe({
      next: (updated) => {
        if (contrato.parcelas) {
          const idx = contrato.parcelas.findIndex(p => p.id === updated.id);
          if (idx !== -1) {
            contrato.parcelas[idx] = updated;
            contrato.totalRecebido -= parcela.valor;
            contrato.totalPendente += parcela.valor;
          }
        }
      },
      error: (err) => console.error('Erro ao desfazer recebimento:', err)
    });
  }

  // ─── Formatters ─────────────────────────────────────────────────────────

  formatMoney(value: number): string {
    if (!value && value !== 0) return '0,00';
    return value.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    const parts = dateStr.split('-');
    if (parts.length < 3) return dateStr;
    return `${parts[2]}/${parts[1]}/${parts[0]}`;
  }

  formatCnj(cnj: string): string {
    const clean = cnj?.replace(/\D/g, '') || '';
    if (clean.length === 20) {
      return `${clean.substring(0, 7)}-${clean.substring(7, 9)}.${clean.substring(9, 13)}.${clean.substring(13, 14)}.${clean.substring(14, 16)}.${clean.substring(16)}`;
    }
    return cnj || '';
  }

  getStatusClass(status: string): string {
    if (status === 'RECEBIDO') return 'badge-success';
    if (status === 'ATRASADO') return 'badge-danger';
    return 'badge-warning';
  }

  formatStatus(status: string): string {
    const map: any = { PENDENTE: 'Pendente', RECEBIDO: 'Recebido', ATRASADO: 'Atrasado' };
    return map[status] || status;
  }
}
