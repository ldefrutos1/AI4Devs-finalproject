<script setup lang="ts">
import { nextTick, onBeforeUnmount, useId, useTemplateRef, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { AiChatTurn } from '@/types/ai'

const props = withDefaults(
  defineProps<{
    messages?: AiChatTurn[]
    isLoading?: boolean
    error?: string
    canRetry?: boolean
    canSendMessage?: boolean
    isAtThreadLimit?: boolean
    maxContentLength?: number
  }>(),
  {
    messages: () => [],
    isLoading: false,
    error: '',
    canRetry: false,
    canSendMessage: false,
    isAtThreadLimit: false,
    maxContentLength: 2000,
  },
)

const open = defineModel<boolean>('open', { required: true })
const draft = defineModel<string>('draft', { default: '' })

const emit = defineEmits<{
  send: []
  retry: []
}>()

const { t } = useI18n()
const dialogRef = useTemplateRef<HTMLDialogElement>('dialogRef')
const threadRef = useTemplateRef<HTMLElement>('threadRef')
const inputRef = useTemplateRef<HTMLTextAreaElement>('inputRef')
const titleId = useId()
const introId = useId()
const errorId = useId()

function syncDialogToModel(isDialogOpen: boolean): void {
  const el = dialogRef.value
  if (!el) {
    return
  }
  if (isDialogOpen) {
    if (!el.open) {
      try {
        el.showModal()
      } catch {
        el.setAttribute('open', '')
      }
    }
  } else if (el.open) {
    el.close()
  }
}

async function scrollThreadToBottom(): Promise<void> {
  await nextTick()
  const el = threadRef.value
  if (!el) {
    return
  }
  el.scrollTop = el.scrollHeight
}

watch(
  open,
  async (isOpen) => {
    await nextTick()
    syncDialogToModel(isOpen)
    if (isOpen) {
      await nextTick()
      inputRef.value?.focus()
      await scrollThreadToBottom()
    }
  },
  { flush: 'post', immediate: true },
)

watch(
  () => [props.messages.length, props.isLoading] as const,
  () => {
    if (open.value) {
      void scrollThreadToBottom()
    }
  },
)

function onCloseClick(): void {
  open.value = false
}

function onDialogCancel(ev: Event): void {
  ev.preventDefault()
  open.value = false
}

function onSendClick(): void {
  if (!props.canSendMessage || props.isLoading) {
    return
  }
  emit('send')
}

function onRetryClick(): void {
  if (!props.canRetry || props.isLoading) {
    return
  }
  emit('retry')
}

onBeforeUnmount(() => {
  const el = dialogRef.value
  if (el?.open) {
    el.close()
  }
})
</script>

<template>
  <dialog
    ref="dialogRef"
    class="mtl-form-dialog mtl-tree-chat-dialog"
    aria-modal="true"
    :aria-labelledby="titleId"
    :aria-describedby="`${introId}${error ? ` ${errorId}` : ''}`"
    data-testid="tree-chat-dialog"
    @cancel="onDialogCancel"
  >
    <div class="mtl-form-dialog-panel mtl-tree-chat-dialog-panel" @click.stop>
      <header class="mtl-tree-chat-dialog-header">
        <h3 :id="titleId" class="mtl-form-dialog-title mtl-tree-chat-dialog-title">
          {{ t('chat.dialog.title') }}
        </h3>
        <p :id="introId" class="mtl-enrichment-dialog-intro mtl-tree-chat-dialog-intro">
          {{ t('chat.dialog.orientativeNotice') }}
        </p>
      </header>

      <div
        ref="threadRef"
        class="mtl-tree-chat-thread"
        role="log"
        aria-live="polite"
        aria-relevant="additions"
        data-testid="tree-chat-thread"
      >
        <p
          v-if="messages.length === 0 && !isLoading"
          class="mtl-tree-chat-thread-empty status-note"
          data-testid="tree-chat-empty"
        >
          {{ t('chat.dialog.emptyThread') }}
        </p>

        <div
          v-for="(turn, index) in messages"
          :key="`${turn.role}-${index}-${turn.content.slice(0, 24)}`"
          class="mtl-tree-chat-bubble"
          :class="
            turn.role === 'user'
              ? 'mtl-tree-chat-bubble--user'
              : 'mtl-tree-chat-bubble--assistant'
          "
          :data-testid="`tree-chat-message-${turn.role}-${index}`"
        >
          <span class="mtl-tree-chat-bubble__role">
            {{
              turn.role === 'user'
                ? t('chat.dialog.roles.user')
                : t('chat.dialog.roles.assistant')
            }}
          </span>
          <p class="mtl-tree-chat-bubble__content">{{ turn.content }}</p>
        </div>

        <div
          v-if="isLoading"
          class="mtl-tree-chat-bubble mtl-tree-chat-bubble--assistant mtl-tree-chat-bubble--loading"
          data-testid="tree-chat-loading"
          aria-live="polite"
        >
          <span class="mtl-tree-chat-bubble__role">{{ t('chat.dialog.roles.assistant') }}</span>
          <p class="mtl-tree-chat-bubble__content status-note">{{ t('chat.dialog.loading') }}</p>
        </div>
      </div>

      <div
        v-if="error"
        :id="errorId"
        class="mtl-tree-chat-error mtl-alert mtl-alert--error"
        role="alert"
        data-testid="tree-chat-error"
      >
        <p class="mtl-tree-chat-error__message">{{ error }}</p>
        <button
          v-if="canRetry"
          type="button"
          class="btn btn-secondary btn-sm"
          data-testid="tree-chat-retry"
          :disabled="isLoading"
          @click="onRetryClick"
        >
          {{ t('chat.dialog.retry') }}
        </button>
      </div>

      <p
        v-if="isAtThreadLimit"
        class="mtl-tree-chat-limit status-note"
        data-testid="tree-chat-thread-limit"
        role="status"
      >
        {{ t('chat.dialog.threadLimit') }}
      </p>

      <form class="mtl-tree-chat-composer" data-testid="tree-chat-composer" @submit.prevent="onSendClick">
        <label class="form-label" for="tree-chat-input">
          {{ t('chat.dialog.inputLabel') }}
        </label>
        <textarea
          id="tree-chat-input"
          ref="inputRef"
          v-model="draft"
          class="form-control form-textarea mtl-tree-chat-input"
          rows="3"
          :maxlength="maxContentLength"
          :placeholder="t('chat.dialog.inputPlaceholder')"
          :disabled="isLoading || isAtThreadLimit"
          data-testid="tree-chat-input"
        />
        <div class="mtl-tree-chat-composer-actions mtl-form-dialog-actions">
          <button
            type="button"
            class="btn btn-secondary"
            data-testid="tree-chat-close"
            :aria-label="t('chat.dialog.closeAria')"
            @click="onCloseClick"
          >
            {{ t('chat.dialog.close') }}
          </button>
          <button
            type="submit"
            class="btn btn-primary tree-form-submit"
            data-testid="tree-chat-send"
            :disabled="!canSendMessage || isLoading"
          >
            {{ isLoading ? t('chat.dialog.sending') : t('chat.dialog.send') }}
          </button>
        </div>
      </form>
    </div>
  </dialog>
</template>
