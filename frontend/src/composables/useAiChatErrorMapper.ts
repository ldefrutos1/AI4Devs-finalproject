import { useI18n } from 'vue-i18n'
import { mapAiChatError, type AiChatErrorMessages } from '@/services/ai/aiChatErrors'

export function useAiChatErrorMapper() {
  const { t } = useI18n()

  const messages: AiChatErrorMessages = {
    networkError: t('chat.ai.errors.networkError'),
    unauthorized: t('chat.ai.errors.unauthorized'),
    badRequest: t('chat.ai.errors.badRequest'),
    forbidden: t('chat.ai.errors.forbidden'),
    tooManyRequests: t('chat.ai.errors.tooManyRequests'),
    badGateway: t('chat.ai.errors.badGateway'),
    serviceError: t('chat.ai.errors.serviceError'),
    unexpectedError: t('chat.ai.errors.unexpectedError'),
  }

  function toMessage(error: unknown): string {
    return mapAiChatError(error, messages)
  }

  return { toMessage }
}
