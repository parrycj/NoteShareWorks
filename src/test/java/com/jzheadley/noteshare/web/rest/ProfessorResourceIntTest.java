package com.jzheadley.noteshare.web.rest;

import com.jzheadley.noteshare.NoteShareApp;

import com.jzheadley.noteshare.domain.Professor;
import com.jzheadley.noteshare.repository.ProfessorRepository;
import com.jzheadley.noteshare.repository.search.ProfessorSearchRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ProfessorResource REST controller.
 *
 * @see ProfessorResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = NoteShareApp.class)
public class ProfessorResourceIntTest {

    private static final String DEFAULT_FIRST_NAME = "AAAAA";
    private static final String UPDATED_FIRST_NAME = "BBBBB";

    private static final String DEFAULT_LAST_NAME = "AAAAA";
    private static final String UPDATED_LAST_NAME = "BBBBB";

    @Inject
    private ProfessorRepository professorRepository;

    @Inject
    private ProfessorSearchRepository professorSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restProfessorMockMvc;

    private Professor professor;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ProfessorResource professorResource = new ProfessorResource();
        ReflectionTestUtils.setField(professorResource, "professorSearchRepository", professorSearchRepository);
        ReflectionTestUtils.setField(professorResource, "professorRepository", professorRepository);
        this.restProfessorMockMvc = MockMvcBuilders.standaloneSetup(professorResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Professor createEntity(EntityManager em) {
        Professor professor = new Professor()
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME);
        return professor;
    }

    @Before
    public void initTest() {
        professorSearchRepository.deleteAll();
        professor = createEntity(em);
    }

    @Test
    @Transactional
    public void createProfessor() throws Exception {
        int databaseSizeBeforeCreate = professorRepository.findAll().size();

        // Create the Professor

        restProfessorMockMvc.perform(post("/api/professors")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(professor)))
                .andExpect(status().isCreated());

        // Validate the Professor in the database
        List<Professor> professors = professorRepository.findAll();
        assertThat(professors).hasSize(databaseSizeBeforeCreate + 1);
        Professor testProfessor = professors.get(professors.size() - 1);
        assertThat(testProfessor.getFirstName()).isEqualTo(DEFAULT_FIRST_NAME);
        assertThat(testProfessor.getLastName()).isEqualTo(DEFAULT_LAST_NAME);

        // Validate the Professor in ElasticSearch
        Professor professorEs = professorSearchRepository.findOne(testProfessor.getId());
        assertThat(professorEs).isEqualToComparingFieldByField(testProfessor);
    }

    @Test
    @Transactional
    public void checkLastNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = professorRepository.findAll().size();
        // set the field null
        professor.setLastName(null);

        // Create the Professor, which fails.

        restProfessorMockMvc.perform(post("/api/professors")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(professor)))
                .andExpect(status().isBadRequest());

        List<Professor> professors = professorRepository.findAll();
        assertThat(professors).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllProfessors() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professors
        restProfessorMockMvc.perform(get("/api/professors?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(professor.getId().intValue())))
                .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME.toString())))
                .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME.toString())));
    }

    @Test
    @Transactional
    public void getProfessor() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get the professor
        restProfessorMockMvc.perform(get("/api/professors/{id}", professor.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(professor.getId().intValue()))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRST_NAME.toString()))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LAST_NAME.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingProfessor() throws Exception {
        // Get the professor
        restProfessorMockMvc.perform(get("/api/professors/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateProfessor() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);
        professorSearchRepository.save(professor);
        int databaseSizeBeforeUpdate = professorRepository.findAll().size();

        // Update the professor
        Professor updatedProfessor = professorRepository.findOne(professor.getId());
        updatedProfessor
                .firstName(UPDATED_FIRST_NAME)
                .lastName(UPDATED_LAST_NAME);

        restProfessorMockMvc.perform(put("/api/professors")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedProfessor)))
                .andExpect(status().isOk());

        // Validate the Professor in the database
        List<Professor> professors = professorRepository.findAll();
        assertThat(professors).hasSize(databaseSizeBeforeUpdate);
        Professor testProfessor = professors.get(professors.size() - 1);
        assertThat(testProfessor.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
        assertThat(testProfessor.getLastName()).isEqualTo(UPDATED_LAST_NAME);

        // Validate the Professor in ElasticSearch
        Professor professorEs = professorSearchRepository.findOne(testProfessor.getId());
        assertThat(professorEs).isEqualToComparingFieldByField(testProfessor);
    }

    @Test
    @Transactional
    public void deleteProfessor() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);
        professorSearchRepository.save(professor);
        int databaseSizeBeforeDelete = professorRepository.findAll().size();

        // Get the professor
        restProfessorMockMvc.perform(delete("/api/professors/{id}", professor.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean professorExistsInEs = professorSearchRepository.exists(professor.getId());
        assertThat(professorExistsInEs).isFalse();

        // Validate the database is empty
        List<Professor> professors = professorRepository.findAll();
        assertThat(professors).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchProfessor() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);
        professorSearchRepository.save(professor);

        // Search the professor
        restProfessorMockMvc.perform(get("/api/_search/professors?query=id:" + professor.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(professor.getId().intValue())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME.toString())))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME.toString())));
    }
}
