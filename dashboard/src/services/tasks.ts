import { httpClient, normalizeError } from './http'
import type { Task, TaskStatus, TaskPriority } from '../types/task'

export interface ListTasksParams {
  status?: TaskStatus
  priority?: TaskPriority
  q?: string
  page: number
  size: number
  sort: 'updatedAt,ASC' | 'updatedAt,DESC'
}

export interface ListTasksResponse {
  items: Task[]
  total: number
  page: number
  size: number
}

// Spring Page response structure
interface SpringPageResponse {
  content: Task[]
  totalElements: number
  number: number
  size: number
  totalPages: number
}

export const list = async (params: ListTasksParams, signal?: AbortSignal): Promise<ListTasksResponse> => {
  try {
    const response = await httpClient.get<SpringPageResponse>('/tasks', { params, signal })
    const pageData = response.data

    // Map Spring Page structure to our expected structure
    return {
      items: pageData.content,
      total: pageData.totalElements,
      page: pageData.number + 1, // Convert back to 1-based for frontend
      size: pageData.size
    }
  } catch (error) {
    throw new Error(normalizeError(error))
  }
}

export const create = async (payload: Partial<Task> & { title: string }): Promise<Task> => {
  try {
    const response = await httpClient.post<Task>('/tasks', payload)
    return response.data
  } catch (error) {
    throw new Error(normalizeError(error))
  }
}

export const update = async (id: string | number, patch: Partial<Task>): Promise<Task> => {
  try {
    const response = await httpClient.patch<Task>(`/tasks/${id}`, patch)
    return response.data
  } catch (error) {
    throw new Error(normalizeError(error))
  }
}

export const remove = async (id: string | number): Promise<void> => {
  try {
    await httpClient.delete(`/tasks/${id}`)
  } catch (error) {
    throw new Error(normalizeError(error))
  }
}
