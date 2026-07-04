export const es = {
  common: {
    cancel: 'Cancelar',
    emptyValue: '—',
    filtersTitle: 'Filtros de búsqueda',
    filters: {
      speciesUnresolved:
        'No se ha seleccionado ninguna especie. Elige una de la lista o escribe el nombre completo.',
    },
  },
  appShell: {
    brand: 'MyTreeLibrary',
    tagline: 'Catálogo colaborativo de árboles singulares',
  },
  navigation: {
    ariaLabel: 'Navegación principal',
    pageBackNavAria: 'Volver a la pantalla anterior',
    exploreSection: 'Explorar',
    workspaceSection: 'Mi espacio',
    adminSection: 'Administración',
    openMenu: 'Abrir menú',
    closeMenu: 'Cerrar menú',
    home: 'Inicio',
    trees: 'Catálogo',
    subscribe: 'Suscripción',
    createTree: 'Alta de ejemplar',
    myTrees: 'Mis árboles',
    adminMasters: 'Maestros',
    adminSubscriptions: 'Suscripciones',
    login: 'Iniciar sesión',
    logout: 'Cerrar sesión',
  },
  home: {
    panelNavAria: 'Accesos del panel',
    collaboratorTitle: 'Panel de colaborador',
    collaboratorDescription: 'Gestiona el catálogo y crea nuevas fichas de árboles.',
    adminTitle: 'Panel de administrador',
    adminDescription: 'Gestiona maestros, suscripciones y la operación del catálogo.',
    authInitializing: 'Inicializando la sesión...',
    publicSectionTitle: 'Bienvenido',
    visitorHeroDescription:
      'Explora el catálogo colaborativo de árboles singulares, recibe avisos por correo o inicia sesión para colaborar con la comunidad.',
    /** Texto alternativo del hero ilustrado; complementa el h2 visible */
    dashboardHeroIllustrationAlt: 'Ilustración decorativa junto al título de esta sección.',
    collaboratorSectionTitle: 'Acciones de colaborador',
    adminSectionTitle: 'Acciones de administración',
    login: 'Iniciar sesión con Keycloak',
    logout: 'Cerrar sesión',
    tiles: {
      createTree: {
        title: 'Alta de ficha',
        desc: 'Registrar un árbol singular con fotos, ubicación y datos de la ficha.',
      },
      myTrees: {
        title: 'Mis árboles',
        desc: 'Revisar y gestionar las fichas que has dado de alta como colaborador.',
      },
      masters: {
        title: 'Maestros',
        desc: 'Mantener familias, géneros y especies del catálogo taxonómico.',
      },
      subscriptions: {
        title: 'Suscripciones',
        desc: 'Gestionar altas, bajas y estado de las notificaciones por correo.',
      },
    },
    publicTiles: {
      trees: {
        title: 'Catálogo público',
        desc: 'Explorar árboles publicados.',
      },
      subscribe: {
        title: 'Suscripción por correo',
        desc: 'Recibir avisos sobre novedades.',
      },
      login: {
        title: 'Iniciar sesión',
        desc: 'Accede con Keycloak para colaborar o administrar.',
      },
    },
  },
  login: {
    title: 'Redirigiendo a Keycloak...',
    description: 'Te estamos enviando al inicio de sesión seguro.',
    redirecting: 'Redirigiendo…',
  },
  authCallback: {
    title: 'Completando autenticación...',
    validating: 'Estamos validando tu sesión.',
    processing: 'Procesando la respuesta del proveedor de identidad…',
    error: 'No se pudo completar el inicio de sesión.',
  },
  authGuardError: {
    title: 'No se pudo validar la sesión',
    descriptionSession:
      'No hemos podido contactar con el proveedor de identidad o renovar tu sesión.',
    descriptionForbidden: 'Tu usuario no tiene permisos para acceder a esta pantalla.',
    retryCta: 'Reintentar autenticación',
    backHomeCta: 'Inicio',
    retryError: 'No se pudo iniciar la autenticación. Inténtalo de nuevo en unos segundos.',
  },
  subscriptionNew: {
    title: 'Suscripción por correo',
    intro:
      'Recibirás avisos sobre nuevsa incorporaciones al catálogo.',
    fields: {
      email: {
        label: 'Correo electrónico',
        // En vue-i18n 9+, `@` en el literal inicia sintaxis "linked"; usar token literal.
        placeholder: "tu.correo{'@'}ejemplo.org",
      },
    },
    submit: 'Suscribirme',
    submitting: 'Enviando…',
    success: 'Te has suscrito correctamente con {email}.',
    subscribeAnother: 'Suscribir otro correo',
    errors: {
      emailRequired: 'Indica un correo electrónico.',
      conflictAlreadyActive: 'Este correo ya está suscrito a las notificaciones.',
      conflictCancelled:
        'Esta suscripción está cancelada. Solo un administrador puede reactivarla desde la gestión de suscripciones.',
      conflictGeneric: 'No se pudo completar el alta por un conflicto con el correo indicado.',
      badRequest: 'Los datos enviados no son válidos. Revisa el correo.',
      network:
        'No se pudo conectar con el servicio. Comprueba tu conexión o que el API Gateway esté en marcha.',
      serviceError: 'Error en el servicio (código {status}).',
      unexpected: 'No se pudo completar la suscripción por un error inesperado.',
    },
  },
  adminSubscriptions: {
    title: 'Gestión de suscripciones',
    description:
      'Listado de correos suscritos a avisos del catálogo. Puedes pasar cada suscripción a cancelada o reactivarla; no se borran filas (MVP).',
    listTitle: 'Suscripciones registradas',
    loading: 'Cargando suscripciones…',
    emptyTitle: 'Sin suscripciones',
    empty: 'No hay suscripciones que mostrar con el filtro actual.',
    resultsCount: '{count} resultado(s)',
    filters: {
      email: {
        label: 'Correo electrónico',
        placeholder: 'Texto parcial del correo',
      },
      estado: {
        label: 'Estado',
        all: 'Todos',
        activa: 'Activas',
        cancelada: 'Canceladas',
      },
      apply: 'Aplicar filtro',
      clear: 'Limpiar',
    },
    fields: {
      email: 'Correo',
      estado: 'Estado',
      altaEn: 'Alta',
      confirmadoEn: 'Confirmado',
      bajaEn: 'Baja',
      actions: 'Acciones',
    },
    estado: {
      ACTIVA: 'Activa',
      CANCELADA: 'Cancelada',
    },
    actions: {
      cancel: 'Cancelar',
      reactivate: 'Reactivar',
    },
    pagination: {
      navLabel: 'Paginación de suscripciones',
      previous: 'Anterior',
      next: 'Siguiente',
      pageStatus: 'Página {current} de {total}',
    },
    confirmCancel: '¿Dar de baja la suscripción de {email}? Pasará a estado cancelada.',
    confirmReactivate: '¿Reactivar la suscripción de {email}?',
    modal: {
      titleCancel: 'Dar de baja suscripción',
      titleReactivate: 'Reactivar suscripción',
      confirmCancel: 'Dar de baja',
      confirmReactivate: 'Reactivar',
    },
    messages: {
      patchSuccess: 'Estado actualizado correctamente.',
      network:
        'No se pudo conectar con el servicio. Comprueba el API Gateway y notification-service.',
      badRequest: 'La petición no es válida.',
      unauthorized: 'Tu sesión no es válida o ha caducado. Vuelve a iniciar sesión.',
      forbidden: 'No tienes permisos para esta operación (se requiere rol administrador).',
      notFound: 'No se encontró el listado o el recurso solicitado.',
      patchNotFound: 'No existe la suscripción indicada o ya no está disponible.',
      badGateway:
        'El servicio de notificaciones no está disponible (502/503). Arranca notification-service o revisa el gateway.',
      serviceError: 'Error en el servicio (código {status}).',
      unexpected: 'Ha ocurrido un error inesperado.',
    },
  },
  adminMasters: {
    title: 'Administración de maestros',
    description:
      'Consulta y mantén las especies del catálogo taxonómico. Desde aquí puedes crear, editar o eliminar fichas de especie.',
    loading: 'Cargando datos taxonómicos…',
    listTitle: 'Especies registradas',
    emptyList: 'No hay especies registradas todavía.',
    resultsCount: '{count} resultado(s)',
    loadingSpecies: 'Cargando especies…',
    filters: {
      species: {
        label: 'Especie',
        placeholder: 'Ej.: Encina o Quercus ilex',
      },
      genus: {
        label: 'Género',
        all: 'Todos los géneros',
      },
      apply: 'Aplicar filtro',
      clear: 'Limpiar',
    },
    columns: {
      species: 'Especie',
      genus: 'Género',
      actions: 'Acciones',
    },
    form: {
      createTitle: 'Alta de especie',
      editTitle: 'Editar especie',
      genus: 'Género',
      family: 'Familia',
      selectGenus: 'Selecciona un género',
      selectFamily: 'Selecciona una familia',
      scientificName: 'Nombre científico',
      commonName: 'Nombre común (opcional)',
      addGenus: 'Alta de género',
      addFamily: 'Alta de familia',
    },
    modals: {
      genusTitle: 'Alta de género',
      familyTitle: 'Alta de familia',
    },
    modal: {
      deleteTitle: 'Eliminar especie',
      deleteMessage: '¿Eliminar la especie «{label}»? No se puede deshacer.',
    },
    actions: {
      edit: 'Editar',
      delete: 'Eliminar',
      create: 'Crear',
      save: 'Guardar',
      back: 'Volver',
      cancel: 'Cancelar',
    },
    pagination: {
      navLabel: 'Paginación de especies',
      previous: 'Anterior',
      next: 'Siguiente',
      pageStatus: 'Página {current} de {total}',
    },
    validation: {
      required: 'Completa los campos obligatorios.',
    },
    messages: {
      created: 'Especie creada correctamente.',
      updated: 'Especie actualizada correctamente.',
      deleted: 'Especie eliminada correctamente.',
      genusCreated: 'Género creado; ya está seleccionado en el formulario.',
      familyCreated: 'Familia creada; ya está seleccionada en el modal de género.',
      network: 'No se pudo conectar con el servicio. Comprueba el API Gateway y catalog-service.',
      badRequest: 'La petición no es válida.',
      unauthorized: 'Tu sesión no es válida o ha caducado. Vuelve a iniciar sesión.',
      forbidden: 'No tienes permisos para esta operación (se requiere rol administrador).',
      notFound: 'No se encontró el recurso taxonómico solicitado.',
      conflictDelete:
        'No se puede eliminar la especie porque existen fichas de árbol que la referencian.',
      badGateway:
        'El catálogo no está disponible (502/503). Arranca catalog-service o revisa el gateway.',
      serviceError: 'Error en el servicio (código {status}).',
      unexpectedError: 'Ha ocurrido un error inesperado.',
    },
  },
  pendingViews: {
    default: {
      title: 'Pantalla pendiente',
      description:
        'Esta sección está preparada como placeholder y se completará en su historia funcional.',
      backHome: 'Inicio',
    },
    treesList: {
      title: 'Listado de árboles publicados',
    },
    treesDetail: {
      title: 'Detalle de árbol',
    },
    treesEdit: {
      title: 'Edición de árbol',
    },
    myTrees: {
      title: 'Mis árboles',
    },
    adminMasters: {
      title: 'Administración de maestros',
    },
    adminSubscriptions: {
      title: 'Gestión de suscripciones',
    },
  },
  treesList: {
    title: 'Ejemplares de MyTreeLibrary',
    description: 'Explora fichas publicadas y filtra por especie, municipio o provincia.',
    loading: 'Cargando ejemplares publicados...',
    emptyTitle: 'Sin resultados',
    empty: 'No hay resultados para los filtros seleccionados.',
    imageUnavailable: 'Imagen no disponible',
    viewDetail: 'Ver detalle',
    resultsCount: '{count} resultado(s)',
    fields: {
      province: 'Provincia',
      municipality: 'Municipio',
      state: 'Estado',
      visibility: 'Visibilidad',
    },
    filters: {
      species: {
        label: 'Especie',
        placeholder: 'Ej.: Encina o Quercus ilex',
      },
      municipality: {
        label: 'Municipio',
        placeholder: 'Ej.: Madrid',
      },
      province: {
        label: 'Provincia',
        all: 'Todas las provincias',
      },
      state: {
        label: 'Estado',
        all: 'Todos los estados',
        borrador: 'Borrador',
        publicado: 'Publicado',
      },
      visibility: {
        label: 'Visibilidad en mapa',
        all: 'Todas las visibilidades',
        privado: 'Privado',
        publico: 'Público',
      },
      moreFilters: 'Más filtros',
      fewerFilters: 'Menos filtros',
      apply: 'Aplicar filtros',
      clear: 'Limpiar',
    },
    pagination: {
      navLabel: 'Paginación del listado de árboles',
      previous: 'Anterior',
      next: 'Siguiente',
      pageStatus: 'Página {current} de {total}',
    },
    messages: {
      badRequest: 'Los filtros indicados no son válidos.',
      networkError: 'No se pudo conectar con el servicio. Verifica el entorno local.',
      badGateway:
        'El catálogo no está disponible: el API Gateway no alcanza catalog-service (p. ej. puerto 8081). Arranca el microservicio o revisa la URL del gateway.',
      serviceError: 'Error en el servicio ({status}).',
      unexpectedError: 'No se pudo cargar el listado por un error inesperado.',
    },
  },
  myTrees: {
    title: 'Mis árboles',
    description:
      'Gestiona tus fichas registradas y accede a la edición con los filtros habituales.',
    loading: 'Cargando tus fichas...',
    emptyTitle: 'Sin ejemplares',
    empty: 'No hay resultados para los filtros seleccionados.',
    edit: 'Editar',
    resultsCount: '{count} resultado(s)',
    fields: {
      province: 'Provincia',
      municipality: 'Municipio',
      state: 'Estado',
      visibility: 'Visibilidad',
    },
    filters: {
      species: {
        label: 'Especie',
        placeholder: 'Ej.: Encina o Quercus ilex',
      },
      createdFrom: {
        label: 'Fecha creación desde',
      },
      createdTo: {
        label: 'Fecha creación hasta',
      },
      createdByUserId: {
        label: 'Usuario (ID)',
        placeholder: 'ID de usuario_app',
      },
      moreFilters: 'Más filtros',
      fewerFilters: 'Menos filtros',
      apply: 'Aplicar filtros',
      clear: 'Limpiar',
    },
    pagination: {
      navLabel: 'Paginación de mis árboles',
      previous: 'Anterior',
      next: 'Siguiente',
      pageStatus: 'Página {current} de {total}',
    },
  },
  treesDetail: {
    title: 'Detalle de árbol publicado',
    description: 'Fotografías, mapa y datos publicados de la ficha.',
    backToList: 'Volver al listado',
    loading: 'Cargando detalle del árbol...',
    treeId: 'Ficha #{id}',
    coordinatesPair: '{lat}, {lng}',
    coordinatesWithAltitude: '{lat}, {lng} · {altitude} m',
    sections: {
      media: 'Fotografías y mapa',
      facts: 'Datos de la ficha',
    },
    fields: {
      species: 'Especie',
      province: 'Provincia',
      municipality: 'Municipio',
      state: 'Estado',
      visibility: 'Visibilidad',
      latitude: 'Latitud',
      longitude: 'Longitud',
      altitude: 'Altitud (m)',
      description: 'Descripción',
      coordinates: 'Coordenadas',
    },
    map: {
      title: 'Ubicación en mapa',
      noLocation: 'No hay coordenadas válidas para mostrar la localización en el mapa.',
      ariaReadOnly: 'Mapa de localización del árbol (solo lectura)',
    },
    gallery: {
      title: 'Fotografías',
      noPhotos: 'No hay fotografías disponibles para este árbol.',
      openViewer: 'Abrir visor ampliado de fotografías',
      previous: 'Anterior',
      next: 'Siguiente',
      position: 'Imagen {current} de {total}',
      fullscreenTitle: 'Vista ampliada de fotografías',
      close: 'Cerrar',
      zoomReset: 'Restablecer',
      zoomLevel: 'Zoom {percent}%',
      help: 'Usa la rueda del ratón para ampliar, arrastra para mover y teclas Esc / ← / → / 0.',
    },
    messages: {
      notFound: 'No se ha encontrado una ficha pública con ese identificador.',
      networkError: 'No se pudo conectar con el servicio. Verifica el entorno local.',
      serviceError: 'Error en el servicio ({status}).',
      unexpectedError: 'No se pudo cargar el detalle por un error inesperado.',
    },
    notFoundHint: 'Comprueba el enlace o vuelve al listado de fichas publicadas.',
  },
  collaboratorCatalog: {
    messages: {
      networkError: 'No se pudo conectar con el servicio. Verifica tu conexión o el entorno local.',
      unauthorized: 'Tu sesión no es válida o ha caducado. Inicia sesión de nuevo para continuar.',
      badRequest: 'Los datos enviados no son válidos.',
      forbidden: 'No tienes permiso para realizar esta operación sobre la ficha.',
      notFound: 'No se encontró la ficha de árbol indicada.',
      badGateway:
        'El catálogo no está disponible: el API Gateway no alcanza catalog-service (p. ej. puerto 8081). Arranca el microservicio o revisa la URL del gateway.',
      serviceError: 'Error en el servicio.',
      unexpectedError: 'No se pudo completar la operación por un error inesperado.',
    },
  },
  treeEdit: {
    title: 'Edición Id {id}',
    titleInvalid: 'Edición de árbol',
    assistantTrigger: 'Asistente IA',
    assistantTriggerAria: 'Abrir asistente de chat para esta ficha',
    description: 'Modifica los datos de la ficha, la galería fotográfica y la ubicación en mapa.',
    loading: 'Cargando ficha para edición...',
    backToList: 'Volver a mis árboles',
    save: 'Guardar',
    saving: 'Guardando...',
    delete: 'Eliminar ficha',
    deleting: 'Eliminando...',
    galleryFallbackAlt: 'Fotografía del árbol',
    gallery: {
      deletePhoto: 'Eliminar esta fotografía',
      addPhoto: 'Añadir fotografía',
      noPhotosHint: 'Sin fotografías',
      deleteConfirm: {
        title: 'Eliminar fotografía',
        message: '¿Confirmas la eliminación de esta fotografía? Esta acción no se puede deshacer.',
        confirm: 'Eliminar',
        cancel: 'Cancelar',
      },
    },
    deleteConfirm: {
      title: 'Eliminar ficha',
      message:
        '¿Confirmas la eliminación de esta ficha? Se borrarán también sus fotografías asociadas. Esta acción no se puede deshacer.',
      confirm: 'Eliminar',
      cancel: 'Cancelar',
    },
    messages: {
      invalidId: 'Identificador de ficha no válido.',
      saveSuccess: 'Ficha e información ampliada guardadas correctamente.',
      saveSuccessWithProjectionWarning:
        'Ficha e información ampliada guardadas. Revisa el aviso sobre la proyección en Mongo.',
      createdFromForm: 'Ficha creada correctamente. Revisa los datos y guarda si haces cambios.',
      createdFromFormWithPhotos:
        'Ficha creada y fotografías asociadas. Revisa los datos y guarda si haces cambios.',
      createdFromFormPhotosWarning:
        'La ficha se creó correctamente, pero no se pudieron subir todas las fotografías. Puedes añadirlas aquí.',
    },
  },
  treeForm: {
    title: 'Alta de ejemplar',
    description: 'Completa los campos obligatorios para registrar una nueva ficha en el catálogo.',
    loadingMasters: 'Cargando especies y provincias...',
    submit: 'Crear ficha',
    submitting: 'Guardando...',
    sections: {
      speciesAndVisibility: 'Especie y visibilidad',
      media: 'Fotografías y mapa',
      location: 'Ubicación',
      coordinates: 'Coordenadas',
    },
    fields: {
      species: {
        label: 'Especie *',
        placeholder: 'Selecciona una especie',
      },
      province: {
        label: 'Provincia *',
        placeholder: 'Selecciona una provincia',
      },
      municipality: {
        label: 'Municipio',
        placeholder: 'Ej.: Madrid, Alcobendas, San Lorenzo de El Escorial',
      },
      description: {
        label: 'Descripción',
        placeholder:
          'Opcional. Describe el árbol y el contexto de la observación (máximo 5000 caracteres)',
      },
      latitude: {
        label: 'Latitud *',
        placeholder: 'Ej.: 40.4168 (rango -90 a 90)',
      },
      longitude: {
        label: 'Longitud *',
        placeholder: 'Ej.: -3.7038 (rango -180 a 180)',
      },
      altitude: {
        label: 'Altitud (m)',
        placeholder: 'Opcional. Ej.: 650',
      },
      publicationState: {
        label: 'Estado',
        options: {
          BORRADOR: 'Borrador',
          PUBLICADO: 'Publicado',
        },
      },
      publicMapVisibility: {
        label: 'Visibilidad',
        options: {
          PRIVADO: 'Privado (no visible en mapa público)',
          PUBLICO: 'Público (visible en mapa público)',
        },
      },
    },
    validation: {
      speciesRequired: 'Selecciona una especie.',
      provinceRequired: 'Selecciona una provincia.',
      latitudeRequired: 'La latitud es obligatoria.',
      latitudeRange: 'La latitud debe estar entre -90 y 90.',
      longitudeRequired: 'La longitud es obligatoria.',
      longitudeRange: 'La longitud debe estar entre -180 y 180.',
      descriptionMaxLength: 'La descripción no puede superar 5000 caracteres.',
    },
    map: {
      ariaLabel: 'Mapa de vista previa: doble clic para elegir coordenadas',
      attributionPrefix: 'Datos del mapa © ',
      openStreetMapLabel: 'OpenStreetMap',
      attributionSuffix: ' y colaboradores. Uso de teselas sujeto a la política de OpenStreetMap.',
    },
    photos: {
      title: 'Fotografías',
      help: 'Selecciona hasta {maxPhotos} imágenes ({allowed}), máximo {maxMb} MB por archivo.',
      chooseFiles: 'Elegir archivos…',
      selectedCount: '{count} de {max} seleccionada(s)',
      inputAriaLabel: 'Seleccionar fotografías del árbol',
      empty: 'Todavía no has seleccionado fotografías.',
      mainBadge: 'Foto principal',
      exifApplied: 'Coordenadas actualizadas desde la primera fotografía (EXIF/GPS).',
      remove: 'Quitar',
      validation: {
        maxPhotos: 'Solo se permiten {max} fotografías por árbol.',
        invalidMime: '{fileName}: formato no permitido. Tipos válidos: {allowed}.',
        maxFileSize: '{fileName}: supera el tamaño máximo de {maxMb} MB.',
      },
    },
    messages: {
      mastersEmpty: 'No hay datos maestros disponibles para completar el formulario.',
      created: 'Ficha creada correctamente con id {treeId}.',
      createdWithPhotos:
        'Ficha creada con id {treeId}. Las fotografías se han asociado correctamente.',
      photoStorageUploadFailed:
        'La ficha se creó, pero la subida al almacén de objetos falló (código {status}). Comprueba MinIO/CORS o inténtalo de nuevo.',
      forbidden: 'No tiene permiso para realizar esta operación.',
      unexpectedError: 'No se pudo completar el alta por un error inesperado.',
      networkError: 'No se pudo conectar con el servicio. Verifica tu conexión o el entorno local.',
      unauthorized: 'Tu sesión no es válida o ha caducado. Inicia sesión de nuevo para continuar.',
      badRequest: 'Revisa los datos del formulario; hay campos no válidos.',
      serviceError: 'Error en el servicio ({status}).',
    },
  },
  enrichment: {
    common: {
      guidedMode: 'Modo guiado',
      advancedJsonMode: 'Modo JSON avanzado',
      jsonHint: 'Objeto o lista JSON válida.',
    },
    guided: {
      listHint: 'Varios valores separados por comas.',
      health: {
        assessment: 'Valoración general',
        pests: 'Plagas detectadas',
        lastRevision: 'Última revisión',
        lesions: 'Lesiones',
        addLesion: 'Añadir lesión',
        removeLesion: 'Quitar lesión',
        lesionTitle: 'Lesión {index}',
        lesionType: 'Tipo',
        lesionSide: 'Lado',
        lesionDescription: 'Descripción',
      },
      ecological: {
        habitat: 'Hábitat',
        altitudeMinM: 'Altitud mínima (m)',
        altitudeMaxM: 'Altitud máxima (m)',
        climate: 'Clima',
      },
      references: {
        empty: 'Todavía no hay referencias bibliográficas.',
        addFirstReference: 'Añadir primera referencia',
        addAnotherReference: 'Añadir otra referencia',
        removeReference: 'Quitar esta referencia',
        referenceLabel: 'Referencia {index}',
        title: 'Título',
        authors: 'Autores',
        source: 'Fuente o editorial',
        year: 'Año',
        url: 'Enlace web',
      },
    },
    species: {
      triggerLabel: 'Ver información ampliada de la especie',
      triggerDisabledHint: 'Selecciona una especie para consultar su información ampliada.',
      badgeWithData: 'Con datos',
      badgeEmpty: 'Sin datos',
      dialogTitle: 'Información ampliada de la especie',
      readOnlyNotice: 'Solo consulta. La edición de la especie está reservada al administrador.',
      loading: 'Cargando información de la especie…',
      empty: 'Todavía no hay datos ampliados para esta especie.',
      save: 'Guardar especie',
      saving: 'Guardando…',
      close: 'Cerrar',
      ai: {
        action: 'Consultar sugerencia IA',
        loading: 'Consultando IA…',
        success:
          'Campos precargados con la sugerencia IA. Revisa el contenido antes de guardar.',
      },
      fields: {
        scientificName: 'Nombre científico',
        commonName: 'Nombre común',
        synonyms: 'Sinónimos',
        synonymsHint: 'Un sinónimo por línea.',
        distributionContinents: 'Continentes',
        distributionCountries: 'Países',
        distributionDescription: 'Descripción de la distribución',
        ecologicalData: 'Datos ecológicos',
        references: 'Referencias bibliográficas',
      },
    },
    tree: {
      sectionTitle: 'Información ampliada del ejemplar',
      saveHint: 'Los cambios de esta sección se guardan al pulsar «Guardar ficha».',
      dirtyBadge: 'Modificado',
      summaryEmpty: 'Sin datos ampliados',
      summaryLoading: 'Cargando resumen…',
      summaryParts: {
        measurements: '{count} medida(s)',
        tags: '{count} etiqueta(s)',
        healthStatus: 'Estado sanitario',
        observations: '{count} observación(es)',
      },
      loading: 'Cargando información del ejemplar…',
      empty: 'Todavía no hay datos ampliados para este ejemplar.',
      readOnlyNotice: 'Solo consulta.',
      createUnavailable:
        'Tras crear la ficha podrás editar el enriquecimiento del ejemplar en la pantalla de edición.',
      fields: {
        measurements: 'Medidas',
        heightM: 'Altura (m)',
        trunkDiameterCm: 'Diámetro del tronco (cm)',
        crownDiameterM: 'Diámetro de copa (m)',
        trunkPerimeterCm: 'Perímetro del tronco (cm)',
        tags: 'Etiquetas',
        tagsHint: 'Separadas por comas.',
        healthStatus: 'Estado sanitario',
        observations: 'Observaciones de campo',
        observationsEmpty: 'Todavía no hay observaciones de campo.',
        observationLabel: 'Observación {index}',
        observationDate: 'Fecha',
        observationText: 'Texto',
        observationAuthor: 'Autor',
        addObservation: 'Añadir observación',
        addFirstObservation: 'Añadir primera observación',
        addAnotherObservation: 'Añadir otra observación',
        removeObservation: 'Quitar esta observación',
      },
    },
    validation: {
      invalidJson: 'El JSON indicado no es válido.',
      invalidMeasurement: 'Revisa las medidas: deben ser números finitos.',
    },
    mongoProjectionWarning:
      'La ficha se guardó en el catálogo, pero el enriquecimiento en Mongo está incompleto. Revisa el bloque inferior e inténtalo de nuevo.',
    ai: {
      errors: {
        networkError:
          'No se pudo conectar con el asistente IA. Verifica tu conexión o que ai-assistant-service esté en marcha.',
        unauthorized: 'Tu sesión no es válida o ha caducado. Inicia sesión de nuevo.',
        badRequest: 'Los datos enviados para la consulta IA no son válidos.',
        forbidden: 'Solo un administrador puede consultar sugerencias IA de especie.',
        notFound: 'La IA no devolvió contenido utilizable para esta especie.',
        unprocessableEntity:
          'La respuesta de la IA no superó la validación. No se han precargado datos; revísalos manualmente si obtienes otra sugerencia.',
        badGateway:
          'El asistente IA no está disponible. Arranca ai-assistant-service o revisa el API Gateway.',
        serviceError: 'Error en el servicio de IA.',
        unexpectedError: 'No se pudo completar la consulta IA por un error inesperado.',
      },
    },
    errors: {
      networkError: 'No se pudo conectar con el servicio. Verifica tu conexión o el entorno local.',
      unauthorized: 'Tu sesión no es válida o ha caducado. Inicia sesión de nuevo.',
      badRequest: 'Los datos de enriquecimiento no son válidos.',
      forbidden: 'No tienes permiso para editar este enriquecimiento.',
      notFound: 'No se encontró el enriquecimiento solicitado.',
      badGateway:
        'El catálogo no está disponible. Arranca catalog-service o revisa el API Gateway.',
      serviceError: 'Error en el servicio.',
      unexpectedError: 'No se pudo completar la operación por un error inesperado.',
    },
  },
  chat: {
    dialog: {
      title: 'Asistente IA',
      orientativeNotice:
        'Las respuestas son orientativas y no sustituyen el criterio del colaborador.',
      close: 'Cerrar',
      closeAria: 'Cerrar asistente de chat',
      send: 'Enviar',
      sending: 'Enviando…',
      retry: 'Reintentar',
      loading: 'El asistente está respondiendo…',
      emptyThread: 'Pregunta sobre la ficha o el proceso de edición. El historial se reinicia al cerrar este panel.',
      threadLimit: 'Has alcanzado el máximo de mensajes de esta conversación.',
      inputLabel: 'Tu mensaje',
      inputPlaceholder: 'Escribe tu consulta…',
      roles: {
        user: 'Tú',
        assistant: 'Asistente',
      },
    },
    ai: {
      errors: {
        networkError:
          'No se pudo conectar con el asistente IA. Verifica tu conexión o que ai-assistant-service esté en marcha.',
        unauthorized: 'Tu sesión no es válida o ha caducado. Inicia sesión de nuevo.',
        badRequest: 'El mensaje no es válido para el chat.',
        forbidden: 'No tienes permiso para usar el asistente de chat.',
        tooManyRequests:
          'Has alcanzado el límite de mensajes. Espera unos segundos antes de volver a intentarlo.',
        badGateway:
          'El asistente IA no está disponible. Arranca ai-assistant-service o revisa el API Gateway.',
        serviceError: 'Error en el servicio de chat IA.',
        unexpectedError: 'No se pudo completar el mensaje por un error inesperado.',
      },
    },
  },
} as const

export type MessageSchema = typeof es
