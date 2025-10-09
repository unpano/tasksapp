<template>
  <tr>
    <!-- Title -->
    <td class="title-cell">
      <input
        v-if="editingTitle"
        ref="titleInput"
        v-model="editedTitle"
        type="text"
        class="inline-input"
        @blur="saveTitleEdit"
        @keydown.enter="saveTitleEdit"
        @keydown.esc="cancelTitleEdit"
      />
      <span v-else @click="startTitleEdit" class="editable-text">
        {{ task.title }}
      </span>
    </td>

    <!-- Status -->
    <td>
      <div class="select-wrapper">
        <select
          :value="editedTask.status || task.status"
          @change="updateStatus"
          class="inline-select"
          :disabled="saving"
        >
          <option value="OPEN">Open</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="DONE">Done</option>
        </select>
        <span v-if="saving" class="spinner"></span>
      </div>
    </td>

    <!-- Priority -->
    <td>
      <div class="select-wrapper">
        <select
          :value="editedTask.priority || task.priority"
          @change="updatePriority"
          class="inline-select"
          :disabled="saving"
        >
          <option value="LOW">Low</option>
          <option value="MEDIUM">Medium</option>
          <option value="HIGH">High</option>
        </select>
        <span v-if="saving" class="spinner"></span>
      </div>
    </td>

    <!-- Due Date -->
    <td>
      <div class="date-wrapper">
        <input
          v-if="editingDue"
          ref="dueDateInput"
          v-model="editedDueDate"
          type="date"
          class="inline-input date-input"
          @blur="saveDueDateEdit"
          @keydown.enter="saveDueDateEdit"
          @keydown.esc="cancelDueDateEdit"
        />
        <span v-else>
          <span @click="startDueDateEdit" class="editable-text">
            {{ formatDate(editedTask.dueDate || task.dueDate) }}
          </span>
          <button
            v-if="editedTask.dueDate || task.dueDate"
            @click="clearDueDate"
            class="clear-btn"
            title="Clear due date"
          >
            ✕
          </button>
        </span>
      </div>
    </td>

    <!-- Updated -->
    <td>{{ formatDate(task.updatedAt) }}</td>

    <!-- Actions -->
    <td class="actions-cell">
      <button
        @click="deleteTask"
        class="delete-btn"
        title="Delete task"
        :disabled="saving"
      >
        Delete
      </button>
    </td>
  </tr>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { useTasksStore } from '../stores/tasks'
import { useToastStore } from '../stores/toast'
import { useConfirmStore } from '../stores/confirm'
import type { Task, TaskStatus, TaskPriority } from '../types/task'

const props = defineProps<{
  task: Task
}>()

const tasksStore = useTasksStore()
const toastStore = useToastStore()
const confirmStore = useConfirmStore()

const editingTitle = ref(false)
const editingDue = ref(false)
const editedTitle = ref('')
const editedDueDate = ref('')
const saving = ref(false)

const titleInput = ref<HTMLInputElement | null>(null)
const dueDateInput = ref<HTMLInputElement | null>(null)

const editedTask = computed(() => {
  return tasksStore.editing[props.task.id] || {}
})

const formatDate = (dateString?: string): string => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString()
}

const formatDateForInput = (dateString?: string): string => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toISOString().split('T')[0]
}

// Title editing
const startTitleEdit = () => {
  tasksStore.startEdit(props.task.id)
  editedTitle.value = props.task.title
  editingTitle.value = true
  nextTick(() => {
    titleInput.value?.focus()
  })
}

const saveTitleEdit = async () => {
  if (!editedTitle.value.trim()) {
    toastStore.error('Title cannot be empty')
    editedTitle.value = props.task.title
    editingTitle.value = false
    return
  }

  if (editedTitle.value === props.task.title) {
    editingTitle.value = false
    return
  }

  saving.value = true
  tasksStore.editing[props.task.id] = {
    ...tasksStore.editing[props.task.id],
    title: editedTitle.value
  }

  try {
    await tasksStore.applyEdit(props.task.id)
    toastStore.success('Task updated successfully')
    editingTitle.value = false
  } catch (error) {
    toastStore.error('Failed to update task')
    editedTitle.value = props.task.title
  } finally {
    saving.value = false
  }
}

const cancelTitleEdit = () => {
  editedTitle.value = props.task.title
  editingTitle.value = false
  tasksStore.cancelEdit(props.task.id)
}

// Status editing
const updateStatus = async (event: Event) => {
  const newStatus = (event.target as HTMLSelectElement).value as TaskStatus
  if (newStatus === props.task.status) return

  saving.value = true
  tasksStore.startEdit(props.task.id)
  tasksStore.editing[props.task.id] = {
    ...tasksStore.editing[props.task.id],
    status: newStatus
  }

  try {
    await tasksStore.applyEdit(props.task.id)
    toastStore.success('Status updated successfully')
  } catch (error) {
    toastStore.error('Failed to update status')
  } finally {
    saving.value = false
  }
}

// Priority editing
const updatePriority = async (event: Event) => {
  const newPriority = (event.target as HTMLSelectElement).value as TaskPriority
  if (newPriority === props.task.priority) return

  saving.value = true
  tasksStore.startEdit(props.task.id)
  tasksStore.editing[props.task.id] = {
    ...tasksStore.editing[props.task.id],
    priority: newPriority
  }

  try {
    await tasksStore.applyEdit(props.task.id)
    toastStore.success('Priority updated successfully')
  } catch (error) {
    toastStore.error('Failed to update priority')
  } finally {
    saving.value = false
  }
}

// Due date editing
const startDueDateEdit = () => {
  tasksStore.startEdit(props.task.id)
  editedDueDate.value = formatDateForInput(props.task.dueDate)
  editingDue.value = true
  nextTick(() => {
    dueDateInput.value?.focus()
  })
}

const saveDueDateEdit = async () => {
  const newDueDate = editedDueDate.value ? new Date(editedDueDate.value).toISOString() : undefined
  const currentDueDate = props.task.dueDate

  if (newDueDate === currentDueDate) {
    editingDue.value = false
    return
  }

  saving.value = true
  tasksStore.editing[props.task.id] = {
    ...tasksStore.editing[props.task.id],
    dueDate: newDueDate
  }

  try {
    await tasksStore.applyEdit(props.task.id)
    toastStore.success('Due date updated successfully')
    editingDue.value = false
  } catch (error) {
    toastStore.error('Failed to update due date')
    editedDueDate.value = formatDateForInput(props.task.dueDate)
  } finally {
    saving.value = false
  }
}

const cancelDueDateEdit = () => {
  editedDueDate.value = formatDateForInput(props.task.dueDate)
  editingDue.value = false
  tasksStore.cancelEdit(props.task.id)
}

const clearDueDate = async () => {
  saving.value = true
  tasksStore.startEdit(props.task.id)
  tasksStore.editing[props.task.id] = {
    ...tasksStore.editing[props.task.id],
    dueDate: undefined
  }

  try {
    await tasksStore.applyEdit(props.task.id)
    toastStore.success('Due date cleared')
  } catch (error) {
    toastStore.error('Failed to clear due date')
  } finally {
    saving.value = false
  }
}

// Delete task
const deleteTask = async () => {
  const confirmed = await confirmStore.open({
    title: 'Delete Task',
    message: `Are you sure you want to delete task "${props.task.title}"?`,
    confirmText: 'Delete'
  })

  if (!confirmed) {
    return
  }

  saving.value = true
  try {
    await tasksStore.delete(props.task.id)
    toastStore.success('Task deleted successfully')
  } catch (error) {
    toastStore.error('Failed to delete task')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
tr {
  border-bottom: 1px solid #eee;
  transition: background-color 0.15s ease;
}

tr:hover {
  background: #f9fafb;
}

td {
  padding: 0.75rem 1rem;
  position: relative;
  vertical-align: middle;
}

.title-cell {
  font-weight: 500;
  max-width: 300px;
}

.editable-text {
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  margin: -0.25rem -0.5rem;
  border-radius: 4px;
  display: inline-block;
  min-width: 50px;
  min-height: 1.5rem;
  transition: background-color 0.15s ease;
}

.editable-text:hover {
  background: #f3f4f6;
}

.editable-text:focus {
  outline: 2px solid #4a90e2;
  outline-offset: 2px;
}

.inline-input {
  width: 100%;
  padding: 0.25rem 0.5rem;
  border: 1px solid #4a90e2;
  border-radius: 4px;
  font-size: 0.875rem;
  outline: none;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1);
}

.date-input {
  width: auto;
}

.select-wrapper {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.inline-select {
  padding: 0.25rem 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  background: white;
  cursor: pointer;
  transition: border-color 0.15s ease;
}

.inline-select:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.inline-select:focus {
  outline: none;
  border-color: #4a90e2;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1);
}

.date-wrapper {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.clear-btn {
  background: transparent;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  padding: 0.25rem;
  font-size: 0.875rem;
  line-height: 1;
  transition: color 0.15s ease;
}

.clear-btn:hover {
  color: #dc2626;
}

.clear-btn:focus {
  outline: 2px solid #4a90e2;
  outline-offset: 2px;
  border-radius: 2px;
}

.actions-cell {
  width: 100px;
}

.delete-btn {
  padding: 0.375rem 0.75rem;
  background: #dc2626;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.75rem;
  font-weight: 500;
  transition: background-color 0.15s ease;
}

.delete-btn:hover:not(:disabled) {
  background: #b91c1c;
}

.delete-btn:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.3);
}

.delete-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid #f3f4f6;
  border-top-color: #4a90e2;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  td {
    padding: 0.5rem;
    font-size: 0.875rem;
  }

  .title-cell {
    max-width: 150px;
  }

  .inline-input,
  .inline-select {
    font-size: 0.75rem;
  }

  .delete-btn {
    padding: 0.25rem 0.5rem;
    font-size: 0.625rem;
  }

  .actions-cell {
    width: 70px;
  }
}

@media (max-width: 640px) {
  .editable-text {
    padding: 0.125rem 0.25rem;
    margin: -0.125rem -0.25rem;
  }

  .inline-input {
    padding: 0.125rem 0.25rem;
    font-size: 0.75rem;
  }
}
</style>
