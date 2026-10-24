import { api } from './client';
import type {
  AuthResponse, Comment, Dashboard, Label, PageResponse, Priority, Project, Task, TaskRequest, TaskStatus, User,
} from './types';

export const authApi = {
  login: (email: string, password: string) =>
    api.post<AuthResponse>('/auth/login', { email, password }).then((r) => r.data),
  register: (name: string, email: string, password: string) =>
    api.post<AuthResponse>('/auth/register', { name, email, password }).then((r) => r.data),
  me: () => api.get<User>('/auth/me').then((r) => r.data),
};

export const usersApi = {
  list: () => api.get<User[]>('/users').then((r) => r.data),
  adminList: () => api.get<User[]>('/admin/users').then((r) => r.data),
};

export const projectsApi = {
  list: () => api.get<Project[]>('/projects').then((r) => r.data),
  get: (id: number) => api.get<Project>(`/projects/${id}`).then((r) => r.data),
  create: (name: string, description: string) =>
    api.post<Project>('/projects', { name, description }).then((r) => r.data),
  update: (id: number, name: string, description: string) =>
    api.put<Project>(`/projects/${id}`, { name, description }).then((r) => r.data),
  remove: (id: number) => api.delete(`/projects/${id}`),
};

export interface TaskFilters {
  projectId?: number;
  status?: TaskStatus | '';
  priority?: Priority | '';
  q?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export const tasksApi = {
  list: (filters: TaskFilters) => {
    const params = Object.fromEntries(
      Object.entries(filters).filter(([, v]) => v !== '' && v !== undefined),
    );
    return api.get<PageResponse<Task>>('/tasks', { params }).then((r) => r.data);
  },
  get: (id: number) => api.get<Task>(`/tasks/${id}`).then((r) => r.data),
  subtasks: (id: number) => api.get<Task[]>(`/tasks/${id}/subtasks`).then((r) => r.data),
  create: (body: TaskRequest) => api.post<Task>('/tasks', body).then((r) => r.data),
  update: (id: number, body: TaskRequest) => api.put<Task>(`/tasks/${id}`, body).then((r) => r.data),
  setStatus: (id: number, status: TaskStatus) =>
    api.patch<Task>(`/tasks/${id}/status`, { status }).then((r) => r.data),
  remove: (id: number) => api.delete(`/tasks/${id}`),
};

export const labelsApi = {
  list: () => api.get<Label[]>('/labels').then((r) => r.data),
  create: (name: string, color: string) => api.post<Label>('/labels', { name, color }).then((r) => r.data),
  remove: (id: number) => api.delete(`/labels/${id}`),
};

export const commentsApi = {
  list: (taskId: number) => api.get<Comment[]>(`/tasks/${taskId}/comments`).then((r) => r.data),
  add: (taskId: number, content: string) =>
    api.post<Comment>(`/tasks/${taskId}/comments`, { content }).then((r) => r.data),
  remove: (id: number) => api.delete(`/comments/${id}`),
};

export const dashboardApi = {
  get: () => api.get<Dashboard>('/dashboard').then((r) => r.data),
};
