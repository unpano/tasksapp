<template>
  <div class="quick-add-container">
    <h2 class="quick-add-title">Quick Add Task</h2>

    <form @submit.prevent="handleSubmit" class="quick-add-form">
      <div class="form-group">
        <label for="title">Title <span class="required">*</span></label>
        <input
          id="title"
          v-model="tasksStore.creating.title"
          type="text"
          placeholder="Enter task title..."
          @keydown.enter="handleSubmit"
          :class="{ 'error': showTitleError }"
          :aria-invalid="showTitleError"
          aria-describedby="title-error"
        />
        <span v-if="showTitleError" id="title-error" class="error-text" role="alert">Title is required</span>
      </div>

      <div class="form-group">
        <label for="status">Status</label>
        <select id="status" v-model="tasksStore.creating.status">
          <option value="OPEN">Open</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="DONE">Done</option>
        </select>
      </div>

      <div class="form-group">
        <label for="priority">Priority</label>
        <select id="priority" v-model="tasksStore.creating.priority">
          <option value="LOW">Low</option>
          <option value="MEDIUM">Medium</option>
          <option value="HIGH">High</option>
        </select>
      </div>

      <div class="form-group">
        <label for="dueDate">Due Date</label>
        <input
          id="dueDate"
          v-model="dueDate"
          type="date"
        />
      </div>

      <button type="submit" class="add-btn" :disabled="isSubmitting">
        {{ isSubmitting ? 'Adding...' : 'Add Task' }}
      </button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useTasksStore } from '../stores/tasks'

const tasksStore = useTasksStore()
const showTitleError = ref(false)
const isSubmitting = ref(false)
const dueDate = ref<string>('')

// Watch due date and sync to store
watch(dueDate, (newDate) => {
  if (newDate) {
    // Convert to ISO string for the store
    tasksStore.creating.dueDate = new Date(newDate).toISOString()
  } else {
    tasksStore.creating.dueDate = undefined
  }
})

const handleSubmit = async () => {
  showTitleError.value = false

  // Validate title
  if (!tasksStore.creating.title.trim()) {
    showTitleError.value = true
    return
  }

  isSubmitting.value = true

  try {
    await tasksStore.quickCreate()
    // Reset due date input
    dueDate.value = ''
    showTitleError.value = false
  } catch (error) {
    // Error is already set in store
    console.error('Failed to create task:', error)
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.quick-add-container {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.quick-add-title {
  margin: 0 0 1rem 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #111827;
}

.quick-add-form {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr auto;
  gap: 1rem;
  align-items: start;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.form-group label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
}

.required {
  color: #dc2626;
}

.form-group input,
.form-group select {
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 0.875rem;
  background: white;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #4a90e2;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1);
}

.form-group input.error {
  border-color: #dc2626;
}

.error-text {
  font-size: 0.75rem;
  color: #dc2626;
  margin-top: 0.25rem;
}

.add-btn {
  align-self: end;
  padding: 0.5rem 1.5rem;
  background: #4a90e2;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: background 0.2s;
  white-space: nowrap;
}

.add-btn:hover:not(:disabled) {
  background: #357abd;
}

.add-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 1024px) {
  .quick-add-form {
    grid-template-columns: 1fr;
  }

  .add-btn {
    align-self: stretch;
  }
}

@media (max-width: 768px) {
  .quick-add-container {
    padding: 1rem;
  }

  .quick-add-title {
    font-size: 1.125rem;
  }
}
</style>
