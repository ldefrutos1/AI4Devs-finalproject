import { useI18n } from 'vue-i18n'
import {
  mapAiSuggestionError,
  type AiSuggestionErrorMessages,
} from '@/services/ai/aiSuggestionErrors'

export function useAiSuggestionErrorMapper() {
  const { t } = useI18n()

  const messages: AiSuggestionErrorMessages = {
    networkError: t('enrichment.ai.errors.networkError'),
    unauthorized: t('enrichment.ai.errors.unauthorized'),
    badRequest: t('enrichment.ai.errors.badRequest'),
    forbidden: t('enrichment.ai.errors.forbidden'),
    notFound: t('enrichment.ai.errors.notFound'),
    unprocessableEntity: t('enrichment.ai.errors.unprocessableEntity'),
    badGateway: t('enrichment.ai.errors.badGateway'),
    serviceError: t('enrichment.ai.errors.serviceError'),
    unexpectedError: t('enrichment.ai.errors.unexpectedError'),
  }

  function toMessage(error: unknown): string {
    return mapAiSuggestionError(error, messages)
  }

  return { toMessage }
}
