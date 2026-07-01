import { computed, onUnmounted, ref, watch, type ComputedRef, type Ref } from 'vue'
import { isAbortError, useAbortableRequest } from '@/composables/useAbortableRequest'
import { useAiChatErrorMapper } from '@/composables/useAiChatErrorMapper'
import { isAiChatRetryableError } from '@/services/ai/aiChatErrors'
import { sendChatMessage } from '@/services/ai/chatMessageService'
import {
  AI_CHAT_MAX_CONTENT_LENGTH,
  AI_CHAT_MAX_MESSAGES,
  type AiChatTurn,
} from '@/types/ai'

function generateConversationId(): string {
  return crypto.randomUUID()
}

export interface UseTreeChatOptions {
  treeId: Ref<number | null> | ComputedRef<number | null>
}

export function useTreeChat(options: UseTreeChatOptions) {
  const { toMessage } = useAiChatErrorMapper()
  const { runWithAbort, cancel } = useAbortableRequest()

  const conversationId = ref<string | null>(null)
  const messages = ref<AiChatTurn[]>([])
  const draft = ref('')
  const isLoading = ref(false)
  const error = ref('')
  const lastFailedError = ref<unknown>(null)
  const isOpen = ref(false)

  const canRetry = computed(
    () => isAiChatRetryableError(lastFailedError.value) && !isLoading.value && isOpen.value,
  )

  const isAtThreadLimit = computed(() => messages.value.length >= AI_CHAT_MAX_MESSAGES)

  const hasPendingUserTurn = computed(() => messages.value.at(-1)?.role === 'user')

  const canSendMessage = computed(() => {
    if (isLoading.value || !isOpen.value) {
      return false
    }
    const treeId = options.treeId.value
    if (treeId == null || treeId < 1) {
      return false
    }
    if (isAtThreadLimit.value) {
      return false
    }
    if (hasPendingUserTurn.value) {
      return false
    }
    const text = draft.value.trim()
    return text.length > 0 && text.length <= AI_CHAT_MAX_CONTENT_LENGTH
  })

  function resetThread(): void {
    cancel()
    conversationId.value = null
    messages.value = []
    draft.value = ''
    error.value = ''
    lastFailedError.value = null
    isLoading.value = false
  }

  function openChat(): void {
    resetThread()
    conversationId.value = generateConversationId()
    isOpen.value = true
  }

  function closeChat(): void {
    isOpen.value = false
    resetThread()
  }

  watch(
    () => options.treeId.value,
    (next, prev) => {
      if (prev != null && next !== prev) {
        closeChat()
      }
    },
  )

  onUnmounted(() => {
    closeChat()
  })

  async function sendMessage(): Promise<void> {
    if (!canSendMessage.value || isLoading.value) {
      return
    }
    if (messages.value.at(-1)?.role === 'user') {
      return
    }

    const treeId = options.treeId.value
    const convId = conversationId.value
    if (treeId == null || treeId < 1 || !convId) {
      return
    }

    const content = draft.value.trim()
    if (!content) {
      return
    }

    const userTurn: AiChatTurn = { role: 'user', content }
    const outboundMessages = [...messages.value, userTurn]
    if (outboundMessages.length > AI_CHAT_MAX_MESSAGES) {
      return
    }

    messages.value = outboundMessages
    draft.value = ''
    error.value = ''
    lastFailedError.value = null
    isLoading.value = true

    try {
      const response = await runWithAbort((signal) =>
        sendChatMessage(
          {
            conversationId: convId,
            treeId,
            messages: outboundMessages,
          },
          signal,
        ),
      )
      messages.value = [
        ...outboundMessages,
        { role: 'assistant', content: response.message.content },
      ]
    } catch (err: unknown) {
      if (isAbortError(err)) {
        return
      }
      error.value = toMessage(err)
      lastFailedError.value = err
    } finally {
      isLoading.value = false
    }
  }

  async function retryLastTurn(): Promise<void> {
    if (!canRetry.value || isLoading.value) {
      return
    }

    const treeId = options.treeId.value
    const convId = conversationId.value
    const thread = messages.value
    if (treeId == null || treeId < 1 || !convId || thread.length === 0) {
      return
    }
    if (thread.at(-1)?.role !== 'user') {
      return
    }

    error.value = ''
    lastFailedError.value = null
    isLoading.value = true

    try {
      const response = await runWithAbort((signal) =>
        sendChatMessage(
          {
            conversationId: convId,
            treeId,
            messages: thread,
          },
          signal,
        ),
      )
      messages.value = [
        ...thread,
        { role: 'assistant', content: response.message.content },
      ]
    } catch (err: unknown) {
      if (isAbortError(err)) {
        return
      }
      error.value = toMessage(err)
      lastFailedError.value = err
    } finally {
      isLoading.value = false
    }
  }

  return {
    conversationId,
    messages,
    draft,
    isLoading,
    error,
    isOpen,
    canRetry,
    canSendMessage,
    isAtThreadLimit,
    maxContentLength: AI_CHAT_MAX_CONTENT_LENGTH,
    openChat,
    closeChat,
    resetThread,
    sendMessage,
    retryLastTurn,
  }
}
