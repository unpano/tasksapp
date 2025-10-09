import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface Toast {
  id: number
  message: string
  type: 'success' | 'error' | 'info'
}

export const useToastStore = defineStore('toast', () => {
  const toasts = ref<Toast[]>([])
  let nextId = 0

  const show = (message: string, type: 'success' | 'error' | 'info' = 'info', duration = 3000) => {
    const id = nextId++
    const toast: Toast = { id, message, type }
    toasts.value.push(toast)

    if (duration > 0) {
      setTimeout(() => {
        remove(id)
      }, duration)
    }
  }

  const remove = (id: number) => {
    const index = toasts.value.findIndex(t => t.id === id)
    if (index !== -1) {
      toasts.value.splice(index, 1)
    }
  }

  const success = (message: string) => show(message, 'success')
  const error = (message: string) => show(message, 'error')
  const info = (message: string) => show(message, 'info')

  return {
    toasts,
    show,
    remove,
    success,
    error,
    info
  }
})
