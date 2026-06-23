import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

interface ResumoResponse {
  aReceber: number;
  emAtraso: number;
  recebido: number;
  qtdAtrasadas: number;
}

interface FluxoCaixaItem {
  data: string;
  totalEsperado: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dashboard-container animate-fade-in" style="display: flex; flex-direction: column; gap: 2rem;">
      
      <!-- Metrics Cards -->
      <section class="metrics-grid">
        <!-- A Receber no Mês -->
        <div class="card metric-card warning">
          <span class="metric-label">A Receber no Mês</span>
          <span class="metric-value">R$ {{ formatMoney(resumo.aReceber) }}</span>
        </div>

        <!-- Em Atraso -->
        <div class="card metric-card danger">
          <span class="metric-label">Em Atraso</span>
          <span class="metric-value" style="color: var(--danger);">R$ {{ formatMoney(resumo.emAtraso) }}</span>
        </div>

        <!-- Recebido no Mês -->
        <div class="card metric-card success">
          <span class="metric-label">Recebido no Mês</span>
          <span class="metric-value" style="color: var(--success);">R$ {{ formatMoney(resumo.recebido) }}</span>
        </div>
      </section>

      <!-- Cash Flow Chart Card -->
      <section class="card">
        <h3 style="margin-bottom: 0.5rem; font-size: 1.15rem; color: var(--text-primary);">
          Fluxo de Caixa — Projeção para os Próximos 30 Dias
        </h3>
        <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 1.5rem;">
          Passe o mouse sobre as barras para ver a projeção detalhada por dia.
        </p>
        
        <div class="chart-container-wrapper animate-fade-in">
          <!-- Y-Axis Labels -->
          <div class="chart-y-axis">
            <span class="y-label">R$ {{ formatMoneyShort(maxAmount) }}</span>
            <span class="y-label">R$ {{ formatMoneyShort(maxAmount / 2) }}</span>
            <span class="y-label">R$ 0</span>
          </div>

          <!-- Chart Grid Area -->
          <div class="chart-grid-container">
            <!-- Dashed Grid Lines -->
            <div class="chart-grid-line top"></div>
            <div class="chart-grid-line middle"></div>
            <div class="chart-grid-line bottom"></div>

            <!-- Flex Columns -->
            <div class="chart-flex-container">
              <div *ngFor="let item of fluxoCaixa; let i = index" class="chart-column">
                <!-- Bar Wrapper -->
                <div class="chart-bar-wrapper">
                  <div 
                    *ngIf="item.totalEsperado > 0" 
                    [style.height.%]="(item.totalEsperado / maxAmount) * 100" 
                    class="chart-bar"
                  ></div>
                  
                  <!-- Tooltip -->
                  <div class="chart-tooltip">
                    <span class="tooltip-amount">R$ {{ formatMoney(item.totalEsperado) }}</span>
                    <span class="tooltip-date">{{ formatDateFull(item.data) }}</span>
                  </div>
                </div>
                
                <!-- Axis Label -->
                <div class="chart-label">
                  <span *ngIf="shouldShowLabelForIndex(i)">
                    {{ formatDateShort(item.data) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Recent Activities / Quick Information -->
      <section style="width: 100%;">
        <div class="card">
          <h4 style="margin-bottom: 1rem; color: var(--text-primary);">Avisos e Lembretes</h4>
          <ul style="list-style: none; display: flex; flex-direction: column; gap: 0.75rem;">
            <li *ngIf="resumo.qtdAtrasadas > 0" class="alert alert-danger" style="padding: 0.75rem 1rem;">
              <span>
                <strong>Atenção:</strong> {{ resumo.qtdAtrasadas }} 
                {{ resumo.qtdAtrasadas === 1 ? 'parcela de honorários está' : 'parcelas de honorários estão' }} em atraso.
              </span>
            </li>
            <li class="alert alert-success" style="padding: 0.75rem 1rem; background-color: rgba(142, 70, 45, 0.05);">
              <span>Regime de caixa ativo: todas as datas refletem recebimentos reais.</span>
            </li>
          </ul>
        </div>
      </section>

    </div>
  `
})
export class DashboardComponent implements OnInit {
  resumo: ResumoResponse = {
    aReceber: 0,
    emAtraso: 0,
    recebido: 0,
    qtdAtrasadas: 0
  };

  fluxoCaixa: FluxoCaixaItem[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.fetchResumo();
    this.fetchFluxoCaixa();
  }

  fetchResumo() {
    this.http.get<ResumoResponse>('/api/dashboard/resumo').subscribe({
      next: (data) => {
        this.resumo = data;
      },
      error: (err) => {
        console.error('Erro ao buscar resumo do dashboard:', err);
      }
    });
  }

  fetchFluxoCaixa() {
    this.http.get<FluxoCaixaItem[]>('/api/dashboard/fluxo-caixa').subscribe({
      next: (data) => {
        this.fluxoCaixa = data;
      },
      error: (err) => {
        console.error('Erro ao buscar fluxo de caixa do dashboard:', err);
      }
    });
  }

  formatMoney(value: number): string {
    return value.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  formatMoneyShort(value: number): string {
    if (value >= 1000) {
      return (value / 1000).toFixed(1).replace('.', ',') + 'k';
    }
    return value.toString();
  }

  formatDateShort(dateStr: string): string {
    const parts = dateStr.split('-');
    if (parts.length < 3) return dateStr;
    return `${parts[2]}/${parts[1]}`;
  }

  formatDateFull(dateStr: string): string {
    const parts = dateStr.split('-');
    if (parts.length < 3) return dateStr;
    return `${parts[2]}/${parts[1]}/${parts[0]}`;
  }

  shouldShowLabelForIndex(i: number): boolean {
    return i % 5 === 0;
  }

  get maxAmount(): number {
    const maxVal = Math.max(...this.fluxoCaixa.map(item => item.totalEsperado), 0);
    return maxVal > 0 ? Math.ceil(maxVal / 1000) * 1000 : 5000;
  }
}
