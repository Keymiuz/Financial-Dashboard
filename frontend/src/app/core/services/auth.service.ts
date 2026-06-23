import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  nomeUsuario: string;
  tenantId: string;
}

export interface RefreshRequest {
  refreshToken: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly ACCESS_TOKEN_KEY = 'lexfinance_access_token';
  private readonly REFRESH_TOKEN_KEY = 'lexfinance_refresh_token';
  private readonly USER_NAME_KEY = 'lexfinance_user_name';
  private readonly USER_EMAIL_KEY = 'lexfinance_user_email';
  private readonly TENANT_ID_KEY = 'lexfinance_tenant_id';
  private readonly TENANT_NAME_KEY = 'lexfinance_tenant_name';

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', request).pipe(
      tap(response => this.handleAuthentication(request.email, response))
    );
  }

  refresh(refreshToken: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/refresh', { refreshToken } as RefreshRequest).pipe(
      tap(response => this.handleAuthentication(this.getUserEmail() || '', response))
    );
  }

  logout(): void {
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_NAME_KEY);
    localStorage.removeItem(this.USER_EMAIL_KEY);
    localStorage.removeItem(this.TENANT_ID_KEY);
    localStorage.removeItem(this.TENANT_NAME_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getAccessToken();
  }

  getAccessToken(): string | null {
    return localStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  getUserName(): string | null {
    return localStorage.getItem(this.USER_NAME_KEY);
  }

  getUserEmail(): string | null {
    return localStorage.getItem(this.USER_EMAIL_KEY);
  }

  getTenantId(): string | null {
    return localStorage.getItem(this.TENANT_ID_KEY);
  }

  getTenantName(): string | null {
    return localStorage.getItem(this.TENANT_NAME_KEY);
  }

  private handleAuthentication(email: string, response: LoginResponse): void {
    localStorage.setItem(this.ACCESS_TOKEN_KEY, response.accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);
    localStorage.setItem(this.USER_NAME_KEY, response.nomeUsuario);
    localStorage.setItem(this.USER_EMAIL_KEY, email);
    localStorage.setItem(this.TENANT_ID_KEY, response.tenantId);
    
    // MVP uses a hardcoded tenant name for simplicity
    localStorage.setItem(this.TENANT_NAME_KEY, 'AdFinance Advocacia e Associados');
  }
}
