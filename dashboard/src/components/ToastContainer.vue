<template>
  <div class="toast-container" aria-live="polite" aria-atomic="true" role="status">
    <div
      v-for="toast in toastStore.toasts"
      :key="toast.id"
      :class="['toast', `toast-${toast.type}`]"
      role="alert"
    >
      <span>{{ toast.message }}</span>
      <button @click="toastStore.remove(toast.id)" class="toast-close" aria-label="Close notification">✕</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useToastStore } from '../stores/toast'

const toastStore = useToastStore()
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 1rem;
  right: 1rem;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-width: 400px;
}

.toast {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-radius: 6px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  animation: slideIn 0.3s ease-out;
  gap: 1rem;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.toast-success {
  background: #10b981;
  color: white;
}

.toast-error {
  background: #ef4444;
  color: white;
}

.toast-info {
  background: #3b82f6;
  color: white;
}

.toast-close {
  background: transparent;
  border: none;
  color: inherit;
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0;
  line-height: 1;
  opacity: 0.8;
}

.toast-close:hover {
  opacity: 1;
}

@media (max-width: 640px) {
  .toast-container {
    right: 0.5rem;
    left: 0.5rem;
    max-width: none;
  }
}
</style>
