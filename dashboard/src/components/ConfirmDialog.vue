<template>
  <Teleport to="body">
    <Transition name="dialog">
      <div v-if="confirmStore.isOpen" class="dialog-overlay" @click="confirmStore.cancel()">
        <div class="dialog-container" @click.stop role="dialog" aria-modal="true">
          <div class="dialog-header">
            <h2>{{ confirmStore.title }}</h2>
          </div>

          <div class="dialog-body">
            <p>{{ confirmStore.message }}</p>
          </div>

          <div class="dialog-footer">
            <button
              ref="cancelBtn"
              @click="confirmStore.cancel()"
              @keydown.tab.shift.prevent="focusConfirm"
              class="dialog-btn dialog-btn-cancel"
            >
              {{ confirmStore.cancelText }}
            </button>
            <button
              ref="confirmBtn"
              @click="confirmStore.confirm()"
              @keydown.tab.prevent="focusCancel"
              class="dialog-btn dialog-btn-confirm"
            >
              {{ confirmStore.confirmText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { useConfirmStore } from '../stores/confirm'

const confirmStore = useConfirmStore()

const cancelBtn = ref<HTMLButtonElement | null>(null)
const confirmBtn = ref<HTMLButtonElement | null>(null)

const focusConfirm = () => {
  confirmBtn.value?.focus()
}

const focusCancel = () => {
  cancelBtn.value?.focus()
}

// Auto-focus cancel button when dialog opens
watch(() => confirmStore.isOpen, (isOpen) => {
  if (isOpen) {
    nextTick(() => {
      cancelBtn.value?.focus()
    })
  }
})

// Handle Escape key
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && confirmStore.isOpen) {
    confirmStore.cancel()
  }
}

// Add/remove event listener
watch(() => confirmStore.isOpen, (isOpen) => {
  if (isOpen) {
    document.addEventListener('keydown', handleKeydown)
  } else {
    document.removeEventListener('keydown', handleKeydown)
  }
})
</script>

<style scoped>
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 1rem;
}

.dialog-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  max-width: 500px;
  width: 100%;
}

.dialog-header {
  padding: 1.5rem 1.5rem 1rem 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.dialog-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #111827;
}

.dialog-body {
  padding: 1.5rem;
}

.dialog-body p {
  margin: 0;
  color: #6b7280;
  line-height: 1.5;
}

.dialog-footer {
  padding: 1rem 1.5rem 1.5rem 1.5rem;
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

.dialog-btn {
  padding: 0.625rem 1.25rem;
  border: none;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.dialog-btn-cancel {
  background: #f3f4f6;
  color: #374151;
}

.dialog-btn-cancel:hover {
  background: #e5e7eb;
}

.dialog-btn-cancel:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(156, 163, 175, 0.3);
}

.dialog-btn-confirm {
  background: #dc2626;
  color: white;
}

.dialog-btn-confirm:hover {
  background: #b91c1c;
}

.dialog-btn-confirm:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.3);
}

/* Transitions */
.dialog-enter-active,
.dialog-leave-active {
  transition: opacity 0.2s ease;
}

.dialog-enter-from,
.dialog-leave-to {
  opacity: 0;
}

.dialog-enter-active .dialog-container,
.dialog-leave-active .dialog-container {
  transition: transform 0.2s ease;
}

.dialog-enter-from .dialog-container,
.dialog-leave-to .dialog-container {
  transform: scale(0.95);
}

@media (max-width: 640px) {
  .dialog-header {
    padding: 1rem 1rem 0.75rem 1rem;
  }

  .dialog-header h2 {
    font-size: 1.125rem;
  }

  .dialog-body {
    padding: 1rem;
  }

  .dialog-footer {
    padding: 0.75rem 1rem 1rem 1rem;
    flex-direction: column-reverse;
  }

  .dialog-btn {
    width: 100%;
  }
}
</style>
