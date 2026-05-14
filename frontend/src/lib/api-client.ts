export interface ApiErrorBody {
  code: string;
  message: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: ApiErrorBody | null;
}

export class ApiClientError extends Error {
  readonly status: number;
  readonly code?: string;
  readonly response?: ApiResponse<unknown>;

  constructor(message: string, status: number, code?: string, response?: ApiResponse<unknown>) {
    super(message);
    this.name = 'ApiClientError';
    this.status = status;
    this.code = code;
    this.response = response;
  }
}

type QueryValue = string | number | boolean | null | undefined;

export type ApiRequestBody =
  | BodyInit
  | Record<string, unknown>
  | unknown[]
  | null
  | undefined;

export interface ApiRequestOptions extends Omit<RequestInit, 'body'> {
  body?: ApiRequestBody;
  query?: Record<string, QueryValue>;
  auth?: boolean;
  redirectOnUnauthorized?: boolean;
  redirectOnForbidden?: boolean;
}

export const ACCESS_TOKEN_STORAGE_KEY = 'healthcenter.accessToken';
export const REFRESH_TOKEN_STORAGE_KEY = 'healthcenter.refreshToken';

const DEFAULT_API_BASE_URL = 'http://localhost:8080';

export function getApiBaseUrl(): string {
  return (process.env.NEXT_PUBLIC_API_BASE_URL || DEFAULT_API_BASE_URL).replace(/\/+$/, '');
}

export function getAccessToken(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }

  return window.localStorage.getItem(ACCESS_TOKEN_STORAGE_KEY);
}

export function getRefreshToken(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }

  return window.localStorage.getItem(REFRESH_TOKEN_STORAGE_KEY);
}

export function setAuthTokens(accessToken: string, refreshToken?: string): void {
  if (typeof window === 'undefined') {
    return;
  }

  window.localStorage.setItem(ACCESS_TOKEN_STORAGE_KEY, accessToken);

  if (refreshToken) {
    window.localStorage.setItem(REFRESH_TOKEN_STORAGE_KEY, refreshToken);
  }
}

export function clearAuthTokens(): void {
  if (typeof window === 'undefined') {
    return;
  }

  window.localStorage.removeItem(ACCESS_TOKEN_STORAGE_KEY);
  window.localStorage.removeItem(REFRESH_TOKEN_STORAGE_KEY);
}

export async function apiRequest<T>(
  path: string,
  options: ApiRequestOptions = {},
): Promise<T> {
  const response = await apiResponse<T>(path, options);

  if (!response.success) {
    throw new ApiClientError(
      response.error?.message || 'API request failed',
      200,
      response.error?.code,
      response as ApiResponse<unknown>,
    );
  }

  return response.data as T;
}

export async function apiResponse<T>(
  path: string,
  options: ApiRequestOptions = {},
): Promise<ApiResponse<T>> {
  const {
    body,
    headers,
    query,
    auth = true,
    redirectOnUnauthorized = true,
    redirectOnForbidden = true,
    ...init
  } = options;

  const requestHeaders = new Headers(headers);
  const requestBody = normalizeBody(body, requestHeaders);

  if (auth) {
    const accessToken = getAccessToken();
    if (accessToken) {
      requestHeaders.set('Authorization', `Bearer ${accessToken}`);
    }
  }

  const httpResponse = await fetch(buildUrl(path, query), {
    ...init,
    body: requestBody,
    headers: requestHeaders,
  });

  const apiBody = await parseApiResponse<T>(httpResponse);

  if (httpResponse.status === 401) {
    clearAuthTokens();
    if (redirectOnUnauthorized && typeof window !== 'undefined') {
      window.location.href = '/login';
    }
  }

  if (httpResponse.status === 403 && redirectOnForbidden && typeof window !== 'undefined') {
    const from = encodeURIComponent(window.location.pathname);
    window.location.href = `/access-denied?from=${from}`;
  }

  if (!httpResponse.ok) {
    throw new ApiClientError(
      apiBody.error?.message || httpResponse.statusText || 'API request failed',
      httpResponse.status,
      apiBody.error?.code,
      apiBody as ApiResponse<unknown>,
    );
  }

  return apiBody;
}

function buildUrl(path: string, query?: Record<string, QueryValue>): string {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  const url = new URL(`${getApiBaseUrl()}${normalizedPath}`);

  if (query) {
    Object.entries(query).forEach(([key, value]) => {
      if (value !== null && value !== undefined) {
        url.searchParams.set(key, String(value));
      }
    });
  }

  return url.toString();
}

function normalizeBody(body: ApiRequestBody, headers: Headers): BodyInit | undefined {
  if (body === null || body === undefined) {
    return undefined;
  }

  if (
    typeof body === 'string' ||
    body instanceof Blob ||
    body instanceof FormData ||
    body instanceof URLSearchParams ||
    body instanceof ArrayBuffer
  ) {
    return body;
  }

  if (!headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  return JSON.stringify(body);
}

async function parseApiResponse<T>(response: Response): Promise<ApiResponse<T>> {
  if (response.status === 204) {
    return { success: response.ok, data: null, error: null };
  }

  const text = await response.text();
  if (!text) {
    return { success: response.ok, data: null, error: null };
  }

  try {
    return JSON.parse(text) as ApiResponse<T>;
  } catch {
    return {
      success: false,
      data: null,
      error: {
        code: 'INVALID_JSON_RESPONSE',
        message: 'API response is not valid JSON.',
      },
    };
  }
}
