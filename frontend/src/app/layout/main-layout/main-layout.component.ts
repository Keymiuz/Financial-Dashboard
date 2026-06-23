import { Component, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  template: `
    <div class="app-container animate-fade-in">
      <!-- Sidebar -->
      <aside class="sidebar">
        <div class="sidebar-brand">
          <h1>AdFinance</h1>
        </div>
        <nav class="sidebar-nav">
          <a routerLink="/dashboard" routerLinkActive="nav-item-active" class="nav-item">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="9"></rect><rect x="14" y="3" width="7" height="5"></rect><rect x="14" y="12" width="7" height="9"></rect><rect x="3" y="16" width="7" height="5"></rect></svg>
            Dashboard
          </a>
          <a routerLink="/clientes" routerLinkActive="nav-item-active" class="nav-item">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
            Clientes
          </a>
          <a routerLink="/processos" routerLinkActive="nav-item-active" class="nav-item">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
            Processos
          </a>
          <a routerLink="/honorarios" routerLinkActive="nav-item-active" class="nav-item">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="1" x2="12" y2="23"></line><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path></svg>
            Honorários
          </a>
        </nav>
        
        <div class="sidebar-footer">
          <div class="user-info">
            <span class="user-name" [title]="userName">{{ userName }}</span>
            <span class="user-email" [title]="userEmail">{{ userEmail }}</span>
          </div>
          <button (click)="logout()" class="btn-logout">Sair</button>
        </div>
      </aside>

      <!-- Main Section -->
      <div style="display: flex; flex-direction: column; flex: 1; min-height: 100vh;">
        <!-- Header -->
        <header class="header">
          <div class="header-title">
            <h2>{{ getPageTitle() }}</h2>
          </div>
          <div class="tenant-badge" *ngIf="tenantName">
            <span class="tenant-dot"></span>
            <span class="tenant-name">{{ tenantName }}</span>
          </div>
        </header>

        <!-- Main Content Area -->
        <main class="main-content">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `
})
export class MainLayoutComponent implements OnInit {
  userName: string = 'Carregando...';
  userEmail: string = '';
  tenantName: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {
    this.userName = (this.authService.getUserName() || 'Usuário').replace(/LexFinance/gi, 'AdFinance');
    this.userEmail = this.authService.getUserEmail() || '';
    const rawName = this.authService.getTenantName() || 'Escritório Padrão';
    // Normalize any lingering "LexFinance" from old login sessions
    this.tenantName = rawName.replace(/LexFinance/gi, 'AdFinance');
  }

  getPageTitle(): string {
    const url = this.router.url;
    if (url.includes('/dashboard')) return 'Dashboard Financeiro';
    if (url.includes('/clientes')) return 'Cadastro de Clientes';
    if (url.includes('/processos')) return 'Processos Judiciais';
    if (url.includes('/honorarios')) return 'Gestão de Honorários';
    return 'AdFinance';
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
