import {
  apiRequest,
  setAuthTokens,
} from './api-client';
import type { User, UserRole } from './mock-data';

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

export interface CompleteSocialSignupInput {
  completionToken: string;
  email: string;
  name: string;
  rememberLogin?: boolean;
}

export interface SocialAuthResult {
  success: boolean;
  user?: User;
  error?: string;
}

export async function completeSocialSignup(
  input: CompleteSocialSignupInput,
): Promise<SocialAuthResult> {
  try {
    const result = await apiRequest<LoginApiResponse>('/api/auth/social/signup', {
      method: 'POST',
      auth: false,
      redirectOnUnauthorized: false,
      body: {
        completionToken: input.completionToken,
        email: input.email,
        name: input.name,
      },
    });

    setAuthTokens(result.accessToken, result.refreshToken, input.rememberLogin === true);

    return {
      success: true,
      user: toUser(result.member),
    };
  } catch (error) {
    return {
      success: false,
      error: getErrorMessage(error),
    };
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

  return '소셜 회원가입을 완료할 수 없습니다.';
}
