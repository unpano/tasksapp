import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface ConfirmOptions {
  title: string
  message: string
  confirmText?: string
  cancelText?: string
}

export const useConfirmStore = defineStore('confirm', () => {
  const isOpen = ref(false)
  const title = ref('')
  const message = ref('')
  const confirmText = ref('Confirm')
  const cancelText = ref('Cancel')

  let resolvePromise: ((value: boolean) => void) | null = null

  const open = (options: ConfirmOptions): Promise<boolean> => {
    title.value = options.title
    message.value = options.message
    confirmText.value = options.confirmText || 'Confirm'
    cancelText.value = options.cancelText || 'Cancel'
    isOpen.value = true

    return new Promise<boolean>((resolve) => {
      resolvePromise = resolve
    })
  }

  const confirm = () => {
    isOpen.value = false
    if (resolvePromise) {
      resolvePromise(true)
      resolvePromise = null
    }
  }

  const cancel = () => {
    isOpen.value = false
    if (resolvePromise) {
      resolvePromise(false)
      resolvePromise = null
    }
  }

  return {
    isOpen,
    title,
    message,
    confirmText,
    cancelText,
    open,
    confirm,
    cancel
  }
})
