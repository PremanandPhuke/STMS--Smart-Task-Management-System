export type Role = 'USER' | 'ADMIN';
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type Recurrence = 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY';

export const STATUSES: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];
export const PRIORITIES: Priority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];
export const RECURRENCES: Recurrence[] = ['NONE', 'DAILY', 'WEEKLY', 'MONTHLY'];

export const STATUS_LABEL: Record<TaskStatus, string> = {
  TODO: 'To do',
  IN_PROGRESS: 'In progress',
  DONE: 'Done',
};
export const PRIORITY_LABEL: Record<Priority, string> = {
  LOW: 'Low',
  MEDIUM: 'Medium',
  HIGH: 'High',
  URGENT: 'Urgent',
};
export const RECURRENCE_LABEL: Record<Recurrence, string> = {
  NONE: 'Does not repeat',
  DAILY: 'Every day',
  WEEKLY: 'Every week',
  MONTHLY: 'Every month',
};

export interface User {
  id: number;
  name: string;
  email: string;
  role: Role;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface Project {
  id: number;
  name: string;
  description: string | null;
  ownerId: number;
  ownerName: string;
  createdAt: string;
}

export interface Label {
  id: number;
  name: string;
  color: string;
}

export interface Task {
  id: number;
  title: string;
  description: string | null;
  status: TaskStatus;
  priority: Priority;
  recurrence: Recurrence;
  dueDate: string | null;
  priorityScore: number;
  overdue: boolean;
  projectId: number;
  projectName: string;
  assigneeId: number | null;
  assigneeName: string | null;
  parentTaskId: number | null;
  labels: Label[];
  canManage: boolean;
  createdAt: string;
  updatedAt: string;
  completedAt: string | null;
}

export interface TaskRequest {
  title: string;
  description?: string | null;
  status?: TaskStatus;
  priority?: Priority;
  recurrence?: Recurrence;
  dueDate?: string | null;
  projectId?: number;
  assigneeId?: number | null;
  parentTaskId?: number | null;
  labelIds?: number[];
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Comment {
  id: number;
  content: string;
  authorId: number;
  authorName: string;
  createdAt: string;
}

export interface Dashboard {
  total: number;
  todo: number;
  inProgress: number;
  done: number;
  overdue: number;
  dueToday: number;
  dueThisWeek: number;
  completedLast7Days: number;
  openByPriority: Record<string, number>;
  topPriorityTasks: Task[];
}
