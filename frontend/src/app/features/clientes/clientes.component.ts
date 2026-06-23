import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card animate-fade-in">
      <h3 style="margin-bottom: 1rem; color: var(--text-primary);">Clientes</h3>
      <p style="color: var(--text-secondary); margin-bottom: 1.5rem;">
        Esta página exibirá o cadastro de clientes do LexFinance.
      </p>
      <div class="alert alert-success">
        <span>Pronto para implementação futura.</span>
      </div>
    </div>
  `
})
export class ClientesComponent {}
