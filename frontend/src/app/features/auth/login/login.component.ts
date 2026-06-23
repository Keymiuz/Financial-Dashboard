import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-container">
      <div class="login-card animate-fade-in">
        <div class="login-header">
          <div class="login-logo">LexFinance</div>
          <p class="login-subtitle">Acesse o financeiro do seu escritório</p>
        </div>

        <form (ngSubmit)="onSubmit()" #loginForm="ngForm" class="login-form">
          <!-- Alert Box for Errors -->
          <div *ngIf="errorMessage" class="alert alert-danger animate-fade-in" style="margin-bottom: 1.5rem;">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
            <span>{{ errorMessage }}</span>
          </div>

          <!-- Email Field -->
          <div class="form-group">
            <label class="form-label" for="email">E-mail</label>
            <input
              type="email"
              id="email"
              name="email"
              class="form-input"
              placeholder="seuemail@exemplo.com"
              [(ngModel)]="email"
              #emailInput="ngModel"
              required
              email
              [disabled]="loading"
            />
            <span *ngIf="emailInput.touched && emailInput.invalid" class="form-error">
              Por favor, insira um e-mail válido.
            </span>
          </div>

          <!-- Password Field -->
          <div class="form-group" style="margin-bottom: 1.5rem;">
            <label class="form-label" for="password">Senha</label>
            <input
              type="password"
              id="password"
              name="password"
              class="form-input"
              placeholder="••••••••"
              [(ngModel)]="password"
              #passwordInput="ngModel"
              required
              [disabled]="loading"
            />
            <span *ngIf="passwordInput.touched && passwordInput.invalid" class="form-error">
              A senha é obrigatória.
            </span>
          </div>

          <!-- Submit Button -->
          <button 
            type="submit" 
            class="btn btn-primary" 
            [disabled]="loginForm.invalid || loading"
            style="width: 100%; height: 46px;"
          >
            <span *ngIf="!loading">Entrar</span>
            <span *ngIf="loading" style="display: inline-flex; align-items: center; gap: 0.5rem;">
              <!-- Simple CSS Spinner -->
              <svg width="18" height="18" viewBox="0 0 38 38" stroke="currentColor" style="animation: spin 1s linear infinite;">
                <g fill="none" fill-rule="evenodd">
                  <g transform="translate(1 1)" stroke-width="2">
                    <circle stroke-opacity=".5" cx="18" cy="18" r="18"/>
                    <path d="M36 18c0-9.94-8.06-18-18-18"/>
                  </g>
                </g>
              </svg>
              Autenticando...
            </span>
          </button>
        </form>
      </div>
    </div>
    
    <!-- Spinner Keyframes Style -->
    <style>
      @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
      }
    </style>
  `
})
export class LoginComponent {
  email = '';
  password = '';
  loading = false;
  errorMessage = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit() {
    if (!this.email || !this.password) return;

    this.loading = true;
    this.errorMessage = '';

    this.authService.login({ email: this.email, senha: this.password }).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 401) {
          this.errorMessage = 'E-mail ou senha incorretos.';
        } else if (err.status === 403) {
          this.errorMessage = 'Acesso negado para este usuário.';
        } else {
          this.errorMessage = 'Não foi possível conectar ao servidor. Tente novamente mais tarde.';
        }
      }
    });
  }
}
