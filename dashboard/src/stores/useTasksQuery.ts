import { watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useTasksStore } from './tasks'
import type { TaskStatus, TaskPriority } from '../types/task'

let debounceTimer: NodeJS.Timeout | null = null

const isValidTaskStatus = (value: string): value is TaskStatus => {
  return ['OPEN', 'IN_PROGRESS', 'DONE'].includes(value)
}

const isValidTaskPriority = (value: string): value is TaskPriority => {
  return ['LOW', 'MEDIUM', 'HIGH'].includes(value)
}

const isValidSortDir = (value: string): value is 'ASC' | 'DESC' => {
  return ['ASC', 'DESC'].includes(value)
}

export const initializeTasksQuery = () => {
  const router = useRouter()
  const route = useRoute()
  const store = useTasksStore()

  // Parse query params into store on init
  const parseQueryToStore = () => {
    const query = route.query

    // Parse status
    if (typeof query.status === 'string') {
      if (isValidTaskStatus(query.status)) {
        store.filters.status = query.status
      } else if (query.status === 'ALL') {
        store.filters.status = 'ALL'
      }
    }

    // Parse priority
    if (typeof query.priority === 'string') {
      if (isValidTaskPriority(query.priority)) {
        store.filters.priority = query.priority
      } else if (query.priority === 'ALL') {
        store.filters.priority = 'ALL'
      }
    }

    // Parse search query
    if (typeof query.q === 'string') {
      store.filters.q = query.q
    }

    // Parse page
    if (typeof query.page === 'string') {
      const page = parseInt(query.page, 10)
      if (!isNaN(page) && page > 0) {
        store.pagination.page = page
      }
    }

    // Parse size
    if (typeof query.size === 'string') {
      const size = parseInt(query.size, 10)
      if (!isNaN(size) && size > 0) {
        store.pagination.size = size
      }
    }

    // Parse sort
    if (typeof query.sort === 'string') {
      const [field, dir] = query.sort.split(',')
      if (field === 'updatedAt' && isValidSortDir(dir)) {
        store.sort.dir = dir
      }
    }
  }

  // Update query params from store
  const updateQueryFromStore = (debounceQ: boolean = false) => {
    const updateQuery = () => {
      const query: Record<string, string> = {}

      if (store.filters.status !== 'ALL') {
        query.status = store.filters.status
      }

      if (store.filters.priority !== 'ALL') {
        query.priority = store.filters.priority
      }

      if (store.filters.q.trim()) {
        query.q = store.filters.q
      }

      if (store.pagination.page !== 1) {
        query.page = store.pagination.page.toString()
      }

      if (store.pagination.size !== 10) {
        query.size = store.pagination.size.toString()
      }

      if (store.sort.dir !== 'DESC') {
        query.sort = `updatedAt,${store.sort.dir}`
      }

      router.replace({ query })
    }

    if (debounceQ) {
      if (debounceTimer) {
        clearTimeout(debounceTimer)
      }
      debounceTimer = setTimeout(updateQuery, 300)
    } else {
      updateQuery()
    }
  }

  // Initialize from query params
  parseQueryToStore()

  // Fetch initial data
  store.fetchList()

  // Watch for changes and update query params
  watch(() => store.filters.q, () => {
    updateQueryFromStore(true) // Debounce search query
    store.fetchList()
  })

  watch(() => store.filters.status, () => {
    updateQueryFromStore()
    store.fetchList()
  })

  watch(() => store.filters.priority, () => {
    updateQueryFromStore()
    store.fetchList()
  })

  watch(() => store.pagination.page, () => {
    updateQueryFromStore()
    store.fetchList()
  })

  watch(() => store.pagination.size, () => {
    updateQueryFromStore()
    store.fetchList()
  })

  watch(() => store.sort.dir, () => {
    updateQueryFromStore()
    store.fetchList()
  })
}
