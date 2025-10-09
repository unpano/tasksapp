<template>
  <div class="pagination-bar">
    <div class="pagination-info">
      <span>
        Showing {{ startItem }}-{{ endItem }} of {{ tasksStore.total }} tasks
      </span>
    </div>

    <div class="pagination-controls">
      <button
        @click="tasksStore.setPage(tasksStore.pagination.page - 1)"
        :disabled="tasksStore.pagination.page === 1"
        class="pagination-btn"
      >
        Previous
      </button>

      <span class="page-indicator">
        Page {{ tasksStore.pagination.page }} of {{ tasksStore.pageCount || 1 }}
      </span>

      <button
        @click="tasksStore.setPage(tasksStore.pagination.page + 1)"
        :disabled="tasksStore.pagination.page >= tasksStore.pageCount"
        class="pagination-btn"
      >
        Next
      </button>
    </div>

    <div class="page-size-control">
      <label for="page-size">Per page:</label>
      <select
        id="page-size"
        :value="tasksStore.pagination.size"
        @change="tasksStore.setSize(parseInt(($event.target as HTMLSelectElement).value))"
      >
        <option :value="10">10</option>
        <option :value="20">20</option>
        <option :value="50">50</option>
      </select>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useTasksStore } from '../stores/tasks'

const tasksStore = useTasksStore()

const startItem = computed(() => {
  if (tasksStore.total === 0) return 0
  return (tasksStore.pagination.page - 1) * tasksStore.pagination.size + 1
})

const endItem = computed(() => {
  const end = tasksStore.pagination.page * tasksStore.pagination.size
  return Math.min(end, tasksStore.total)
})
</script>

<style scoped>
.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f9fafb;
  border-radius: 8px;
  margin-top: 1rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.pagination-info {
  font-size: 0.875rem;
  color: #6b7280;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.pagination-btn {
  padding: 0.5rem 1rem;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
  color: #374151;
  transition: all 0.2s;
}

.pagination-btn:hover:not(:disabled) {
  background: #f3f4f6;
  border-color: #9ca3af;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-indicator {
  font-size: 0.875rem;
  color: #374151;
  font-weight: 500;
}

.page-size-control {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.page-size-control label {
  font-size: 0.875rem;
  color: #6b7280;
}

.page-size-control select {
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 0.875rem;
  background: white;
}

.page-size-control select:focus {
  outline: none;
  border-color: #4a90e2;
}

@media (max-width: 640px) {
  .pagination-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .pagination-info,
  .pagination-controls,
  .page-size-control {
    justify-content: center;
  }
}
</style>
