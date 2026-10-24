import axios, { AxiosError } from 'axios';
import type { InternalAxiosRequestConfig } from 'axios';
import type { AuthResponse } from './types';

const API_URL: string = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

const ACCESS_KEY = 'stms_access_token';
const REFRESH_KEY = 'stms_refresh_token';

export const getAccessToken = () => localStorage.getItem(ACCESS_KEY);
export const getRefreshToken = () => localStorage.getItem(REFRESH_KEY);
export const saveTokens = (access: string, refresh: string) => {
  localStorage.setItem(ACCESS_KEY, access);
  localStorage.setItem(REFRESH_KEY, refresh);
};
export const clearTokens = () => {
  localStorage.removeItem(ACCESS_KEY);
  localStorage.removeItem(REFRESH_KEY);
};

export const api = axios.create({ baseURL: `${API_URL}/api` });

// 1) Attach the access token to every request
api.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// 2) When the access token has expired (401), use the refresh token once and retry the request
let refreshing: Promise<string> | null = null;

async function refreshAccessToken(): Promise<string> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) throw new Error('No refresh token');
  const { data } = await axios.post<AuthResponse>(`${API_URL}/api/auth/refresh`, { refreshToken });
  saveTokens(data.accessToken, data.refreshToken);
  return data.accessToken;
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retried?: boolean }) | undefined;
    const isAuthCall = /\/auth\/(login|register|refresh)/.test(original?.url ?? '');
    if (error.response?.status === 401 && original && !original._retried && !isAuthCall) {
      original._retried = true;
      try {
        refreshing = refreshing ?? refreshAccessToken().finally(() => (refreshing = null));
        const newToken = await refreshing;
        original.headers.Authorization = `Bearer ${newToken}`;
        return api(original);
      } catch {
        clearTokens();
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  },
);

/** Turns any error into a readable message for the UI. */
export function errorMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    const data = err.response?.data as { message?: string; fieldErrors?: Record<string, string> } | undefined;
    if (data?.fieldErrors) return Object.values(data.fieldErrors).join(', ');
    if (data?.message) return data.message;
    if (!err.response) return 'Cannot reach the server. Is the backend running on port 8080?';
    return err.message;
  }
  return 'Something went wrong';
}
