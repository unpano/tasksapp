<template>
  <div class="tasks-table-container">
    <!-- Error Banner -->
    <div v-if="tasksStore.error" class="error-banner">
      <p>{{ tasksStore.error }}</p>
      <button @click="tasksStore.fetchList()" class="retry-btn">Retry</button>
    </div>

    <!-- Table -->
    <div class="table-wrapper">
      <table class="tasks-table" role="table" aria-label="Tasks list">
        <thead>
          <tr>
            <th scope="col">Title</th>
            <th scope="col">Status</th>
            <th scope="col">Priority</th>
            <th scope="col">Due</th>
            <th scope="col">
              <button
                @click="tasksStore.toggleSort()"
                class="sort-btn"
                :aria-label="`Sort by updated date ${tasksStore.sort.dir === 'DESC' ? 'ascending' : 'descending'}`"
              >
                Updated {{ tasksStore.sort.dir === 'DESC' ? '↓' : '↑' }}
              </button>
            </th>
            <th scope="col">Actions</th>
          </tr>
        </thead>
        <tbody>
          <!-- Loading Skeleton -->
          <template v-if="tasksStore.loading">
            <tr v-for="i in tasksStore.pagination.size" :key="`skeleton-${i}`" class="skeleton-row">
              <td><div class="skeleton"></div></td>
              <td><div class="skeleton"></div></td>
              <td><div class="skeleton"></div></td>
              <td><div class="skeleton"></div></td>
              <td><div class="skeleton"></div></td>
              <td><div class="skeleton"></div></td>
            </tr>
          </template>

          <!-- Empty State -->
          <template v-else-if="tasksStore.items.length === 0">
            <tr>
              <td colspan="6" class="empty-state">
                <p>No tasks found</p>
              </td>
            </tr>
          </template>

          <!-- Data Rows -->
          <template v-else>
            <TaskRow v-for="task in tasksStore.items" :key="task.id" :task="task" />
          </template>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useTasksStore } from '../stores/tasks'
import TaskRow from './TaskRow.vue'

const tasksStore = useTasksStore()
</script>

<style scoped>
.tasks-table-container {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.error-banner {
  background: #fee;
  color: #c33;
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #fcc;
}

.error-banner p {
  margin: 0;
}

.retry-btn {
  padding: 0.5rem 1rem;
  background: #c33;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
}

.retry-btn:hover {
  background: #a22;
}

.table-wrapper {
  overflow-x: auto;
}

.tasks-table {
  width: 100%;
  border-collapse: collapse;
}

.tasks-table th,
.tasks-table td {
  padding: 0.75rem 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.tasks-table th {
  background: #f9fafb;
  font-weight: 600;
  font-size: 0.875rem;
  color: #374151;
  white-space: nowrap;
}

.sort-btn {
  background: none;
  border: none;
  color: #374151;
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.sort-btn:hover {
  color: #4a90e2;
}


.empty-state {
  text-align: center;
  padding: 3rem 1rem !important;
  color: #9ca3af;
  font-size: 1rem;
}

.empty-state p {
  margin: 0;
}

.skeleton-row td {
  padding: 0.75rem 1rem;
}

.skeleton {
  height: 1rem;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 4px;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

@media (max-width: 768px) {
  .tasks-table th,
  .tasks-table td {
    padding: 0.5rem;
    font-size: 0.875rem;
  }
}
</style>
