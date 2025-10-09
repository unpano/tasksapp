export type TaskStatus = 'OPEN' | 'IN_PROGRESS' | 'DONE'

export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH'

export interface Task {
  id: number | string
  title: string
  description?: string
  status: TaskStatus
  priority: TaskPriority
  dueDate?: string
  createdAt: string
  updatedAt: string
}
