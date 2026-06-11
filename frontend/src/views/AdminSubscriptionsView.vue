<script setup lang="ts">
import { computed, onMounted, ref, shallowRef } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import MtlConfirmDialog from '@/components/MtlConfirmDialog.vue'
import PageBackLink from '@/components/layout/PageBackLink.vue'
import { useAdminSubscriptionsList } from '@/composables/useAdminSubscriptionsList'
import type {
  EstadoSuscripcion,
  SubscriptionAdminItem,
} from '@/services/notifications/adminSubscriptions'

type SubscriptionConfirmKind = 'cancel' | 'reactivate'

const route = useRoute()
const { t } = useI18n()

const pageTitleKey = computed(() => {
  const metaTitle = route.meta.pageTitleKey
  return typeof metaTitle === 'string' ? metaTitle : 'adminSubscriptions.title'
})

const {
  page,
  filterEstado,
  filterEmail,
  isLoading,
  errorMessage,
  statusMessage,
  items,
  totalElements,
  totalPages,
  patchingId,
  hasPrevious,
  hasNext,
  hasRows,
  load,
  applyFilter,
  goPrevious,
  goNext,
  setEstado,
} = useAdminSubscriptionsList()

const confirmOpen = ref(false)
const confirmKind = ref<SubscriptionConfirmKind>('cancel')
const confirmRow = shallowRef<SubscriptionAdminItem | null>(null)

const confirmTitle = computed(() =>
  confirmKind.value === 'cancel'
    ? t('adminSubscriptions.modal.titleCancel')
    : t('adminSubscriptions.modal.titleReactivate'),
)

const confirmMessage = computed(() => {
  const row = confirmRow.value
  if (!row) {
    return ''
  }
  return confirmKind.value === 'cancel'
    ? t('adminSubscriptions.confirmCancel', { email: row.email })
    : t('adminSubscriptions.confirmReactivate', { email: row.email })
})

const confirmDanger = computed(() => confirmKind.value === 'cancel')

const confirmActionLabel = computed(() =>
  confirmKind.value === 'cancel'
    ? t('adminSubscriptions.modal.confirmCancel')
    : t('adminSubscriptions.modal.confirmReactivate'),
)

const isListStateOk = computed(() => !isLoading.value && !errorMessage.value)

onMounted(() => {
  void load()
})

const displayTotalPages = computed(() => Math.max(1, totalPages.value || 1))
const displayCurrentPage = computed(() => page.value + 1)

function estadoLabel(code: EstadoSuscripcion | string): string {
  if (code === 'ACTIVA') {
    return t('adminSubscriptions.estado.ACTIVA')
  }
  if (code === 'CANCELADA') {
    return t('adminSubscriptions.estado.CANCELADA')
  }
  return code
}

function formatDate(iso: string | null): string {
  if (!iso) {
    return t('common.emptyValue')
  }
  try {
    return new Date(iso).toLocaleString('es-ES', { dateStyle: 'short', timeStyle: 'short' })
  } catch {
    return iso
  }
}

async function clearFilter(): Promise<void> {
  filterEstado.value = ''
  filterEmail.value = ''
  await applyFilter()
}

function onCancel(row: SubscriptionAdminItem): void {
  confirmRow.value = row
  confirmKind.value = 'cancel'
  confirmOpen.value = true
}

function onReactivate(row: SubscriptionAdminItem): void {
  confirmRow.value = row
  confirmKind.value = 'reactivate'
  confirmOpen.value = true
}

function onConfirmModal(): void {
  const row = confirmRow.value
  if (!row) {
    return
  }
  const kind = confirmKind.value
  confirmRow.value = null
  if (kind === 'cancel') {
    void setEstado(row.subscriptionId, 'CANCELADA')
  } else {
    void setEstado(row.subscriptionId, 'ACTIVA')
  }
}

function onDismissModal(): void {
  confirmRow.value = null
}
</script>

<template>
  <div class="catalog-page admin-subscriptions-page">
    <header class="page-header">
      <PageBackLink :to="{ name: 'home' }">{{ t('navigation.home') }}</PageBackLink>
      <h1 class="page-header__title">{{ t(pageTitleKey) }}</h1>
    </header>

    <output
      v-if="statusMessage"
      class="mtl-alert mtl-alert--success tree-form-page__flash"
      aria-live="polite"
    >
      {{ statusMessage }}
    </output>

    <div class="admin-subscriptions-layout">
      <section class="catalog-toolbar" :aria-label="t('adminSubscriptions.filters.apply')">
      <form class="catalog-toolbar__form" @submit.prevent="applyFilter">
        <div class="catalog-toolbar__panel">
          <h2 class="catalog-toolbar__title">{{ t('common.filtersTitle') }}</h2>
          <div class="catalog-toolbar__fields catalog-toolbar__fields--pair">
            <div class="filter-field">
              <label class="form-label" for="admin-sub-filter-email">{{
                t('adminSubscriptions.filters.email.label')
              }}</label>
              <input
                id="admin-sub-filter-email"
                v-model="filterEmail"
                class="form-control"
                type="search"
                autocomplete="off"
                :placeholder="t('adminSubscriptions.filters.email.placeholder')"
              />
            </div>
            <div class="filter-field">
              <label class="form-label" for="admin-sub-filter-estado">{{
                t('adminSubscriptions.filters.estado.label')
              }}</label>
              <select id="admin-sub-filter-estado" v-model="filterEstado" class="form-control">
                <option value="">{{ t('adminSubscriptions.filters.estado.all') }}</option>
                <option value="ACTIVA">{{ t('adminSubscriptions.filters.estado.activa') }}</option>
                <option value="CANCELADA">
                  {{ t('adminSubscriptions.filters.estado.cancelada') }}
                </option>
              </select>
            </div>
          </div>

          <div class="catalog-toolbar__actions">
            <button
              type="button"
              class="btn btn-secondary btn-sm"
              :disabled="isLoading"
              @click="clearFilter"
            >
              {{ t('adminSubscriptions.filters.clear') }}
            </button>
            <button
              type="submit"
              class="btn btn-primary-soft btn-sm catalog-toolbar__submit"
              :disabled="isLoading"
            >
              {{ t('adminSubscriptions.filters.apply') }}
            </button>
          </div>
        </div>
      </form>
    </section>

    <h2 class="tree-detail-panel__title admin-list-section-title">
      {{ t('adminSubscriptions.listTitle') }}
    </h2>

    <p v-if="isLoading" class="status-note">{{ t('adminSubscriptions.loading') }}</p>
    <p v-else-if="errorMessage" class="error" role="alert">{{ errorMessage }}</p>

    <template v-else-if="isListStateOk">
      <div class="mtl-admin-list-toolbar">
        <p class="catalog-results-count muted">
          {{ t('adminSubscriptions.resultsCount', { count: totalElements }) }}
        </p>
      </div>

      <div v-if="!hasRows" class="mtl-empty-state">
        <p class="mtl-empty-state__title">{{ t('adminSubscriptions.emptyTitle') }}</p>
        <p class="mtl-empty-state__text">{{ t('adminSubscriptions.empty') }}</p>
      </div>

      <div v-else class="catalog-toolbar__panel admin-subscriptions-table-panel">
        <div class="mtl-admin-table-wrap">
          <table
            class="mtl-admin-table mtl-admin-table--stack"
            :aria-label="t('adminSubscriptions.listTitle')"
          >
            <thead>
              <tr>
                <th scope="col">{{ t('adminSubscriptions.fields.email') }}</th>
                <th scope="col">{{ t('adminSubscriptions.fields.estado') }}</th>
                <th scope="col">{{ t('adminSubscriptions.fields.altaEn') }}</th>
                <th scope="col">{{ t('adminSubscriptions.fields.confirmadoEn') }}</th>
                <th scope="col">{{ t('adminSubscriptions.fields.bajaEn') }}</th>
                <th scope="col">{{ t('adminSubscriptions.fields.actions') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in items" :key="row.subscriptionId">
                <td :data-label="t('adminSubscriptions.fields.email')">{{ row.email }}</td>
                <td :data-label="t('adminSubscriptions.fields.estado')">
                  {{ estadoLabel(row.estadoSuscripcion) }}
                </td>
                <td :data-label="t('adminSubscriptions.fields.altaEn')">
                  {{ formatDate(row.altaEn) }}
                </td>
                <td :data-label="t('adminSubscriptions.fields.confirmadoEn')">
                  {{ formatDate(row.confirmadoEn) }}
                </td>
                <td :data-label="t('adminSubscriptions.fields.bajaEn')">
                  {{ formatDate(row.bajaEn) }}
                </td>
                <td
                  class="mtl-admin-table__actions"
                  :data-label="t('adminSubscriptions.fields.actions')"
                >
                  <button
                    v-if="row.estadoSuscripcion === 'ACTIVA'"
                    type="button"
                    class="btn btn-outline-danger btn-sm"
                    :disabled="patchingId !== null"
                    @click="onCancel(row)"
                  >
                    {{ t('adminSubscriptions.actions.cancel') }}
                  </button>
                  <button
                    v-else
                    type="button"
                    class="btn btn-outline-primary btn-sm"
                    :disabled="patchingId !== null"
                    @click="onReactivate(row)"
                  >
                    {{ t('adminSubscriptions.actions.reactivate') }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <nav
        v-if="hasRows"
        class="catalog-pagination"
        :aria-label="t('adminSubscriptions.pagination.navLabel')"
      >
        <button
          class="btn btn-secondary btn-sm catalog-pagination__btn"
          type="button"
          :disabled="!hasPrevious || isLoading"
          @click="goPrevious()"
        >
          {{ t('adminSubscriptions.pagination.previous') }}
        </button>
        <span class="catalog-pagination__status">
          {{
            t('adminSubscriptions.pagination.pageStatus', {
              current: displayCurrentPage,
              total: displayTotalPages,
            })
          }}
        </span>
        <button
          class="btn btn-secondary btn-sm catalog-pagination__btn"
          type="button"
          :disabled="!hasNext || isLoading"
          @click="goNext()"
        >
          {{ t('adminSubscriptions.pagination.next') }}
        </button>
      </nav>
    </template>
    </div>

    <MtlConfirmDialog
      v-model:open="confirmOpen"
      :title="confirmTitle"
      :message="confirmMessage"
      :cancel-label="t('common.cancel')"
      :confirm-label="confirmActionLabel"
      :confirm-danger="confirmDanger"
      @confirm="onConfirmModal"
      @cancel="onDismissModal"
    />
  </div>
</template>
