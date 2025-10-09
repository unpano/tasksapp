<template>
  <div class="app-container">
    <header class="app-header">
      <h1>Tasks Dashboard</h1>
    </header>

    <main class="app-main">
      <QuickAddRow />
      <FiltersBar />
      <TasksTable />
      <PaginationBar />
    </main>

    <ToastContainer />
    <ConfirmDialog />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import QuickAddRow from './components/QuickAddRow.vue'
import FiltersBar from './components/FiltersBar.vue'
import TasksTable from './components/TasksTable.vue'
import PaginationBar from './components/PaginationBar.vue'
import ToastContainer from './components/ToastContainer.vue'
import ConfirmDialog from './components/ConfirmDialog.vue'
import { initializeTasksQuery } from './stores/useTasksQuery'

onMounted(() => {
  initializeTasksQuery()
})
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  background: #f3f4f6;
}

.app-header {
  background: white;
  padding: 1.5rem 2rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

h1 {
  margin: 0;
  font-size: 1.75rem;
  font-weight: 600;
  color: #111827;
}

.app-main {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
}

@media (max-width: 768px) {
  .app-header {
    padding: 1rem;
  }

  h1 {
    font-size: 1.5rem;
  }

  .app-main {
    padding: 1rem;
  }
}
</style>
