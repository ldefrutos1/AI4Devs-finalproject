package com.mtl.catalog.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mtl.catalog.config.JwtDecoderConfigTest;
import com.mtl.catalog.infrastructure.persistence.mongo.repository.EjemplarDetalleMongoRepository;
import com.mtl.catalog.infrastructure.persistence.mongo.repository.EspecieDetalleMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Flujos HTTP + PostgreSQL + Mongo de enriquecimiento (HU-015 TASK-08). Solo escenarios que no
 * cubren bien los mocks unitarios/WebMvc.
 */
@Tag("integration")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles({"test", "test-mongo", "test-it-pg-mongo"})
@Import(JwtDecoderConfigTest.class)
@EnabledIf("com.mtl.catalog.integration.support.DockerConditions#dockerDisponible")
class CatalogEnrichmentApiIT {

  private static final long SPECIES_ID = 1L;
  private static final long PROVINCE_ID = 1L;

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine").withInitScript("postgres-init-test.sql");

  @Container static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0");

  @DynamicPropertySource
  static void registerContainers(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.data.mongodb.uri", MONGO::getConnectionString);
    registry.add("mtl.catalog.mongo.enabled", () -> "true");
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private EjemplarDetalleMongoRepository ejemplarDetalleMongoRepository;
  @Autowired private EspecieDetalleMongoRepository especieDetalleMongoRepository;

  private final JsonMapper jsonMapper = JsonMapper.builder().findAndAddModules().build();

  @BeforeEach
  void limpiarMongo() {
    ejemplarDetalleMongoRepository.deleteAll();
    especieDetalleMongoRepository.deleteAll();
  }

  @Test
  void getSpeciesEnrichment_sinMongo_devuelveProyeccionSql() throws Exception {
    mockMvc
        .perform(
            get("/api/catalog/species/{speciesId}/enrichment", SPECIES_ID)
                .headers(auth(JwtDecoderConfigTest.TOKEN_COLABORADOR)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.speciesId").value((int) SPECIES_ID))
        .andExpect(jsonPath("$.scientificName").value("Quercus ilex"))
        .andExpect(jsonPath("$.synonyms").doesNotExist());
  }

  @Test
  void putSpeciesEnrichment_adminPersiste_colaboradorLee() throws Exception {
    mockMvc
        .perform(
            put("/api/catalog/species/{speciesId}/enrichment", SPECIES_ID)
                .headers(auth(JwtDecoderConfigTest.TOKEN_ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"synonyms\":[\"Encina mediterránea\"]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.scientificName").value("Quercus ilex"))
        .andExpect(jsonPath("$.synonyms[0]").value("Encina mediterránea"));

    mockMvc
        .perform(
            get("/api/catalog/species/{speciesId}/enrichment", SPECIES_ID)
                .headers(auth(JwtDecoderConfigTest.TOKEN_COLABORADOR)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.synonyms[0]").value("Encina mediterránea"));

    assertThat(especieDetalleMongoRepository.findById(SPECIES_ID)).isPresent();
  }

  @Test
  void putSpeciesEnrichment_colaborador_devuelve403() throws Exception {
    mockMvc
        .perform(
            put("/api/catalog/species/{speciesId}/enrichment", SPECIES_ID)
                .headers(auth(JwtDecoderConfigTest.TOKEN_COLABORADOR))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"synonyms\":[\"No permitido\"]}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void postTree_proyeccionYEnrichment_flujoColaborador() throws Exception {
    long treeId = crearEjemplarPublicado();

    mockMvc
        .perform(
            get("/api/catalog/trees/{treeId}/enrichment", treeId)
                .headers(auth(JwtDecoderConfigTest.TOKEN_COLABORADOR)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.treeId").value((int) treeId))
        .andExpect(jsonPath("$.speciesId").value((int) SPECIES_ID));

    assertThat(ejemplarDetalleMongoRepository.findById(treeId)).isPresent();

    mockMvc
        .perform(
            put("/api/catalog/trees/{treeId}/enrichment", treeId)
                .headers(auth(JwtDecoderConfigTest.TOKEN_COLABORADOR))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"measurements\":{\"heightM\":18.5},\"tags\":[\"it-enrichment\"]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.measurements.heightM").value(18.5))
        .andExpect(jsonPath("$.tags[0]").value("it-enrichment"));
  }

  @Test
  void putTreeEnrichment_medidaInvalida_devuelve400() throws Exception {
    long treeId = crearEjemplarPublicado();

    mockMvc
        .perform(
            put("/api/catalog/trees/{treeId}/enrichment", treeId)
                .headers(auth(JwtDecoderConfigTest.TOKEN_COLABORADOR))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"measurements\":{\"heightM\":\"no-numerico\"}}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getPublicTreeEnrichment_fichaPublicadaConDatos() throws Exception {
    long treeId = crearEjemplarPublicado();
    mockMvc
        .perform(
            put("/api/catalog/trees/{treeId}/enrichment", treeId)
                .headers(auth(JwtDecoderConfigTest.TOKEN_COLABORADOR))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tags\":[\"publico-it\"]}"))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/api/catalog/public/trees/{treeId}/enrichment", treeId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.treeEnrichment.tags[0]").value("publico-it"));
  }

  private long crearEjemplarPublicado() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/catalog/trees")
                    .headers(auth(JwtDecoderConfigTest.TOKEN_COLABORADOR))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "speciesId": %d,
                          "provinceId": %d,
                          "latitude": 40.4168,
                          "longitude": -3.7038,
                          "publicMapVisibility": "PUBLICO",
                          "publicationState": "PUBLICADO"
                        }
                        """
                            .formatted(SPECIES_ID, PROVINCE_ID)))
            .andExpect(status().isCreated())
            .andReturn();

    JsonNode body = jsonMapper.readTree(result.getResponse().getContentAsByteArray());
    return body.path("treeId").longValue();
  }

  private static HttpHeaders auth(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    return headers;
  }
}
