import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Task, TaskStatus, TaskPriority } from '../types/task'
import * as tasksService from '../services/tasks'

let abortController: AbortController | null = null

export const useTasksStore = defineStore('tasks', () => {
  // State
  const items = ref<Task[]>([])
  const total = ref(0)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const filters = ref<{
    status: TaskStatus | 'ALL'
    priority: TaskPriority | 'ALL'
    q: string
  }>({
    status: 'ALL',
    priority: 'ALL',
    q: ''
  })

  const pagination = ref({
    page: 1,
    size: 10
  })

  const sort = ref<{
    field: 'updatedAt'
    dir: 'DESC' | 'ASC'
  }>({
    field: 'updatedAt',
    dir: 'DESC'
  })

  const editing = ref<Record<string, Partial<Task>>>({})

  const creating = ref<{
    title: string
    status: TaskStatus
    priority: TaskPriority
    dueDate?: string
  }>({
    title: '',
    status: 'OPEN',
    priority: 'MEDIUM'
  })

  // Getters
  const pageCount = computed(() => Math.ceil(total.value / pagination.value.size))

  const hasActiveFilters = computed(() => {
    return filters.value.status !== 'ALL' ||
           filters.value.priority !== 'ALL' ||
           filters.value.q.trim() !== ''
  })

  // Actions
  const fetchList = async () => {
    // Cancel previous request
    if (abortController) {
      abortController.abort()
    }
    abortController = new AbortController()

    loading.value = true
    error.value = null

    try {
      const params: any = {
        page: pagination.value.page - 1, // Backend uses 0-based pagination
        size: pagination.value.size,
        sort: `${sort.value.field},${sort.value.dir}` as 'updatedAt,ASC' | 'updatedAt,DESC'
      }

      // Only add filters if not 'ALL'
      if (filters.value.status !== 'ALL') {
        params.status = filters.value.status
      }
      if (filters.value.priority !== 'ALL') {
        params.priority = filters.value.priority
      }
      if (filters.value.q.trim()) {
        params.q = filters.value.q.trim()
      }

      const response = await tasksService.list(params, abortController.signal)

      items.value = response.items
      total.value = response.total
    } catch (err: any) {
      if (err.name !== 'AbortError' && err.message !== 'canceled') {
        error.value = err.message || 'Failed to fetch tasks'
      }
    } finally {
      loading.value = false
      abortController = null
    }
  }

  const setFilter = (key: keyof typeof filters.value, value: any) => {
    filters.value[key] = value
    pagination.value.page = 1 // Reset to first page on filter change
  }

  const setPage = (n: number) => {
    pagination.value.page = n
  }

  const setSize = (n: number) => {
    pagination.value.size = n
    pagination.value.page = 1 // Reset to first page on size change
  }

  const toggleSort = () => {
    sort.value.dir = sort.value.dir === 'DESC' ? 'ASC' : 'DESC'
    pagination.value.page = 1 // Reset to first page on sort change
  }

  const startEdit = (id: string | number) => {
    const task = items.value.find(t => t.id === id)
    if (task) {
      editing.value[id] = { ...task }
    }
  }

  const applyEdit = async (id: string | number) => {
    const patch = editing.value[id]
    if (!patch) return

    try {
      const updated = await tasksService.update(id, patch)
      const index = items.value.findIndex(t => t.id === id)
      if (index !== -1) {
        items.value[index] = updated
      }
      delete editing.value[id]
    } catch (err: any) {
      error.value = err.message || 'Failed to update task'
      throw err
    }
  }

  const cancelEdit = (id: string | number) => {
    delete editing.value[id]
  }

  const quickCreate = async () => {
    if (!creating.value.title.trim()) {
      error.value = 'Title is required'
      return
    }

    try {
      const payload: any = {
        title: creating.value.title,
        status: creating.value.status,
        priority: creating.value.priority
      }

      if (creating.value.dueDate) {
        payload.dueDate = creating.value.dueDate
      }

      const newTask = await tasksService.create(payload)

      // Reset creating form
      creating.value = {
        title: '',
        status: 'OPEN',
        priority: 'MEDIUM'
      }

      // Refresh list
      await fetchList()

      return newTask
    } catch (err: any) {
      error.value = err.message || 'Failed to create task'
      throw err
    }
  }

  const deleteTask = async (id: string | number) => {
    try {
      await tasksService.remove(id)

      // Remove from local state
      const index = items.value.findIndex(t => t.id === id)
      if (index !== -1) {
        items.value.splice(index, 1)
        total.value--
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to delete task'
      throw err
    }
  }

  return {
    // State
    items,
    total,
    loading,
    error,
    filters,
    pagination,
    sort,
    editing,
    creating,

    // Getters
    pageCount,
    hasActiveFilters,

    // Actions
    fetchList,
    setFilter,
    setPage,
    setSize,
    toggleSort,
    startEdit,
    applyEdit,
    cancelEdit,
    quickCreate,
    delete: deleteTask
  }
})
