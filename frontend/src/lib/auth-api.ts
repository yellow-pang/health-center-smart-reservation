import {
  apiRequest,
  getRefreshToken,
  setAuthTokens,
  clearAuthTokens,
} from './api-client';
import type { User, UserRole } from './mock-data';

export interface LoginCredentials {
  email: string;
  password: string;
}

interface MemberResponse {
  id: number;
  healthCenterId: number | null;
  email: string;
  name: string;
  role: UserRole;
}

interface LoginApiResponse {
  accessToken: string;
  refreshToken: string;
  member: MemberResponse;
}

export interface AuthResult {
  success: boolean;
  user?: User;
  error?: string;
}

const testAccountCredentials: Record<UserRole, LoginCredentials> = {
  CITIZEN: { email: 'citizen@test.com', password: 'password1234' },
  STAFF: { email: 'staff@test.com', password: 'password1234' },
  ADMIN: { email: 'admin@test.com', password: 'password1234' },
};

export async function login(credentials: LoginCredentials): Promise<AuthResult> {
  try {
    const result = await apiRequest<LoginApiResponse>('/api/auth/login', {
      method: 'POST',
      auth: false,
      body: {
        email: credentials.email,
        password: credentials.password,
      },
    });

    setAuthTokens(result.accessToken, result.refreshToken);

    return {
      success: true,
      user: toUser(result.member),
    };
  } catch (error) {
    clearAuthTokens();
    return {
      success: false,
      error: getErrorMessage(error),
    };
  }
}

export async function loginWithRole(role: UserRole): Promise<AuthResult> {
  return login(testAccountCredentials[role]);
}

export async function getCurrentUser(): Promise<User | null> {
  try {
    const member = await apiRequest<MemberResponse>('/api/members/me', {
      method: 'GET',
      redirectOnUnauthorized: false,
    });

    return toUser(member);
  } catch {
    clearAuthTokens();
    return null;
  }
}

export async function logout(): Promise<void> {
  const refreshToken = getRefreshToken();

  try {
    if (refreshToken) {
      await apiRequest<null>('/api/auth/logout', {
        method: 'POST',
        body: { refreshToken },
        redirectOnUnauthorized: false,
      });
    }
  } finally {
    clearAuthTokens();
  }
}

function toUser(member: MemberResponse): User {
  return {
    id: String(member.id),
    email: member.email,
    name: member.name,
    role: member.role,
  };
}

function getErrorMessage(error: unknown): string {
  if (error instanceof Error && error.message) {
    return error.message;
  }

  return '이메일 또는 비밀번호가 올바르지 않습니다.';
}
