import { useI18n } from 'vue-i18n'
import {
  mapEnrichmentError,
  type EnrichmentErrorMessages,
} from '@/services/catalog/enrichmentErrors'

export function useEnrichmentErrorMapper() {
  const { t } = useI18n()

  const messages: EnrichmentErrorMessages = {
    networkError: t('enrichment.errors.networkError'),
    unauthorized: t('enrichment.errors.unauthorized'),
    badRequest: t('enrichment.errors.badRequest'),
    forbidden: t('enrichment.errors.forbidden'),
    notFound: t('enrichment.errors.notFound'),
    badGateway: t('enrichment.errors.badGateway'),
    serviceError: t('enrichment.errors.serviceError'),
    unexpectedError: t('enrichment.errors.unexpectedError'),
  }

  function toMessage(error: unknown): string {
    return mapEnrichmentError(error, messages)
  }

  return { toMessage }
}
