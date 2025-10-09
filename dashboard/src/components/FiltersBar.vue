<template>
  <div class="filters-bar">
    <div class="filter-group">
      <label for="status-filter">Status:</label>
      <select
        id="status-filter"
        :value="tasksStore.filters.status"
        @change="tasksStore.setFilter('status', ($event.target as HTMLSelectElement).value)"
      >
        <option value="ALL">All</option>
        <option value="OPEN">Open</option>
        <option value="IN_PROGRESS">In Progress</option>
        <option value="DONE">Done</option>
      </select>
    </div>

    <div class="filter-group">
      <label for="priority-filter">Priority:</label>
      <select
        id="priority-filter"
        :value="tasksStore.filters.priority"
        @change="tasksStore.setFilter('priority', ($event.target as HTMLSelectElement).value)"
      >
        <option value="ALL">All</option>
        <option value="LOW">Low</option>
        <option value="MEDIUM">Medium</option>
        <option value="HIGH">High</option>
      </select>
    </div>

    <div class="filter-group search-group">
      <label for="search-filter">Search:</label>
      <input
        id="search-filter"
        type="text"
        :value="tasksStore.filters.q"
        @input="tasksStore.setFilter('q', ($event.target as HTMLInputElement).value)"
        placeholder="Search tasks..."
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useTasksStore } from '../stores/tasks'

const tasksStore = useTasksStore()
</script>

<style scoped>
.filters-bar {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.search-group {
  flex: 1;
  min-width: 200px;
}

label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #333;
}

select,
input {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.875rem;
  background: white;
}

select:focus,
input:focus {
  outline: none;
  border-color: #4a90e2;
}

@media (max-width: 640px) {
  .filters-bar {
    flex-direction: column;
  }

  .filter-group {
    width: 100%;
  }
}
</style>
