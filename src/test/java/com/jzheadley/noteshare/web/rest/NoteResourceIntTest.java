package com.jzheadley.noteshare.web.rest;

import com.jzheadley.noteshare.NoteShareApp;

import com.jzheadley.noteshare.domain.Note;
import com.jzheadley.noteshare.repository.NoteRepository;
import com.jzheadley.noteshare.repository.search.NoteSearchRepository;

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
import org.springframework.util.Base64Utils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jzheadley.noteshare.domain.enumeration.Privacy;
/**
 * Test class for the NoteResource REST controller.
 *
 * @see NoteResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = NoteShareApp.class)
public class NoteResourceIntTest {

    private static final byte[] DEFAULT_NOTE_CONTENT = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_NOTE_CONTENT = TestUtil.createByteArray(2, "1");
    private static final String DEFAULT_NOTE_CONTENT_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_NOTE_CONTENT_CONTENT_TYPE = "image/png";

    private static final ZonedDateTime DEFAULT_DATE_SUBMITTED = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_DATE_SUBMITTED = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_DATE_SUBMITTED_STR = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(DEFAULT_DATE_SUBMITTED);

    private static final Integer DEFAULT_OVERALL_RATING = 1;
    private static final Integer UPDATED_OVERALL_RATING = 2;

    private static final Integer DEFAULT_HANDWRITING_RATING = 1;
    private static final Integer UPDATED_HANDWRITING_RATING = 2;

    private static final Integer DEFAULT_USEFULNESS_RATING = 1;
    private static final Integer UPDATED_USEFULNESS_RATING = 2;

    private static final Privacy DEFAULT_PRIVACY = Privacy.PUBLIC;
    private static final Privacy UPDATED_PRIVACY = Privacy.PRIVATE;

    @Inject
    private NoteRepository noteRepository;

    @Inject
    private NoteSearchRepository noteSearchRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restNoteMockMvc;

    private Note note;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        NoteResource noteResource = new NoteResource();
        ReflectionTestUtils.setField(noteResource, "noteSearchRepository", noteSearchRepository);
        ReflectionTestUtils.setField(noteResource, "noteRepository", noteRepository);
        this.restNoteMockMvc = MockMvcBuilders.standaloneSetup(noteResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Note createEntity(EntityManager em) {
        Note note = new Note()
                .noteContent(DEFAULT_NOTE_CONTENT)
                .noteContentContentType(DEFAULT_NOTE_CONTENT_CONTENT_TYPE)
                .dateSubmitted(DEFAULT_DATE_SUBMITTED)
                .overallRating(DEFAULT_OVERALL_RATING)
                .handwritingRating(DEFAULT_HANDWRITING_RATING)
                .usefulnessRating(DEFAULT_USEFULNESS_RATING)
                .privacy(DEFAULT_PRIVACY);
        return note;
    }

    @Before
    public void initTest() {
        noteSearchRepository.deleteAll();
        note = createEntity(em);
    }

    @Test
    @Transactional
    public void createNote() throws Exception {
        int databaseSizeBeforeCreate = noteRepository.findAll().size();

        // Create the Note

        restNoteMockMvc.perform(post("/api/notes")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(note)))
                .andExpect(status().isCreated());

        // Validate the Note in the database
        List<Note> notes = noteRepository.findAll();
        assertThat(notes).hasSize(databaseSizeBeforeCreate + 1);
        Note testNote = notes.get(notes.size() - 1);
        assertThat(testNote.getNoteContent()).isEqualTo(DEFAULT_NOTE_CONTENT);
        assertThat(testNote.getNoteContentContentType()).isEqualTo(DEFAULT_NOTE_CONTENT_CONTENT_TYPE);
        assertThat(testNote.getDateSubmitted()).isEqualTo(DEFAULT_DATE_SUBMITTED);
        assertThat(testNote.getOverallRating()).isEqualTo(DEFAULT_OVERALL_RATING);
        assertThat(testNote.getHandwritingRating()).isEqualTo(DEFAULT_HANDWRITING_RATING);
        assertThat(testNote.getUsefulnessRating()).isEqualTo(DEFAULT_USEFULNESS_RATING);
        assertThat(testNote.getPrivacy()).isEqualTo(DEFAULT_PRIVACY);

        // Validate the Note in ElasticSearch
        Note noteEs = noteSearchRepository.findOne(testNote.getId());
        assertThat(noteEs).isEqualToComparingFieldByField(testNote);
    }

    @Test
    @Transactional
    public void checkNoteContentIsRequired() throws Exception {
        int databaseSizeBeforeTest = noteRepository.findAll().size();
        // set the field null
        note.setNoteContent(null);

        // Create the Note, which fails.

        restNoteMockMvc.perform(post("/api/notes")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(note)))
                .andExpect(status().isBadRequest());

        List<Note> notes = noteRepository.findAll();
        assertThat(notes).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkDateSubmittedIsRequired() throws Exception {
        int databaseSizeBeforeTest = noteRepository.findAll().size();
        // set the field null
        note.setDateSubmitted(null);

        // Create the Note, which fails.

        restNoteMockMvc.perform(post("/api/notes")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(note)))
                .andExpect(status().isBadRequest());

        List<Note> notes = noteRepository.findAll();
        assertThat(notes).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllNotes() throws Exception {
        // Initialize the database
        noteRepository.saveAndFlush(note);

        // Get all the notes
        restNoteMockMvc.perform(get("/api/notes?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(note.getId().intValue())))
                .andExpect(jsonPath("$.[*].noteContentContentType").value(hasItem(DEFAULT_NOTE_CONTENT_CONTENT_TYPE)))
                .andExpect(jsonPath("$.[*].noteContent").value(hasItem(Base64Utils.encodeToString(DEFAULT_NOTE_CONTENT))))
                .andExpect(jsonPath("$.[*].dateSubmitted").value(hasItem(DEFAULT_DATE_SUBMITTED_STR)))
                .andExpect(jsonPath("$.[*].overallRating").value(hasItem(DEFAULT_OVERALL_RATING)))
                .andExpect(jsonPath("$.[*].handwritingRating").value(hasItem(DEFAULT_HANDWRITING_RATING)))
                .andExpect(jsonPath("$.[*].usefulnessRating").value(hasItem(DEFAULT_USEFULNESS_RATING)))
                .andExpect(jsonPath("$.[*].privacy").value(hasItem(DEFAULT_PRIVACY.toString())));
    }

    @Test
    @Transactional
    public void getNote() throws Exception {
        // Initialize the database
        noteRepository.saveAndFlush(note);

        // Get the note
        restNoteMockMvc.perform(get("/api/notes/{id}", note.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(note.getId().intValue()))
            .andExpect(jsonPath("$.noteContentContentType").value(DEFAULT_NOTE_CONTENT_CONTENT_TYPE))
            .andExpect(jsonPath("$.noteContent").value(Base64Utils.encodeToString(DEFAULT_NOTE_CONTENT)))
            .andExpect(jsonPath("$.dateSubmitted").value(DEFAULT_DATE_SUBMITTED_STR))
            .andExpect(jsonPath("$.overallRating").value(DEFAULT_OVERALL_RATING))
            .andExpect(jsonPath("$.handwritingRating").value(DEFAULT_HANDWRITING_RATING))
            .andExpect(jsonPath("$.usefulnessRating").value(DEFAULT_USEFULNESS_RATING))
            .andExpect(jsonPath("$.privacy").value(DEFAULT_PRIVACY.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingNote() throws Exception {
        // Get the note
        restNoteMockMvc.perform(get("/api/notes/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateNote() throws Exception {
        // Initialize the database
        noteRepository.saveAndFlush(note);
        noteSearchRepository.save(note);
        int databaseSizeBeforeUpdate = noteRepository.findAll().size();

        // Update the note
        Note updatedNote = noteRepository.findOne(note.getId());
        updatedNote
                .noteContent(UPDATED_NOTE_CONTENT)
                .noteContentContentType(UPDATED_NOTE_CONTENT_CONTENT_TYPE)
                .dateSubmitted(UPDATED_DATE_SUBMITTED)
                .overallRating(UPDATED_OVERALL_RATING)
                .handwritingRating(UPDATED_HANDWRITING_RATING)
                .usefulnessRating(UPDATED_USEFULNESS_RATING)
                .privacy(UPDATED_PRIVACY);

        restNoteMockMvc.perform(put("/api/notes")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedNote)))
                .andExpect(status().isOk());

        // Validate the Note in the database
        List<Note> notes = noteRepository.findAll();
        assertThat(notes).hasSize(databaseSizeBeforeUpdate);
        Note testNote = notes.get(notes.size() - 1);
        assertThat(testNote.getNoteContent()).isEqualTo(UPDATED_NOTE_CONTENT);
        assertThat(testNote.getNoteContentContentType()).isEqualTo(UPDATED_NOTE_CONTENT_CONTENT_TYPE);
        assertThat(testNote.getDateSubmitted()).isEqualTo(UPDATED_DATE_SUBMITTED);
        assertThat(testNote.getOverallRating()).isEqualTo(UPDATED_OVERALL_RATING);
        assertThat(testNote.getHandwritingRating()).isEqualTo(UPDATED_HANDWRITING_RATING);
        assertThat(testNote.getUsefulnessRating()).isEqualTo(UPDATED_USEFULNESS_RATING);
        assertThat(testNote.getPrivacy()).isEqualTo(UPDATED_PRIVACY);

        // Validate the Note in ElasticSearch
        Note noteEs = noteSearchRepository.findOne(testNote.getId());
        assertThat(noteEs).isEqualToComparingFieldByField(testNote);
    }

    @Test
    @Transactional
    public void deleteNote() throws Exception {
        // Initialize the database
        noteRepository.saveAndFlush(note);
        noteSearchRepository.save(note);
        int databaseSizeBeforeDelete = noteRepository.findAll().size();

        // Get the note
        restNoteMockMvc.perform(delete("/api/notes/{id}", note.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate ElasticSearch is empty
        boolean noteExistsInEs = noteSearchRepository.exists(note.getId());
        assertThat(noteExistsInEs).isFalse();

        // Validate the database is empty
        List<Note> notes = noteRepository.findAll();
        assertThat(notes).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchNote() throws Exception {
        // Initialize the database
        noteRepository.saveAndFlush(note);
        noteSearchRepository.save(note);

        // Search the note
        restNoteMockMvc.perform(get("/api/_search/notes?query=id:" + note.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(note.getId().intValue())))
            .andExpect(jsonPath("$.[*].noteContentContentType").value(hasItem(DEFAULT_NOTE_CONTENT_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].noteContent").value(hasItem(Base64Utils.encodeToString(DEFAULT_NOTE_CONTENT))))
            .andExpect(jsonPath("$.[*].dateSubmitted").value(hasItem(DEFAULT_DATE_SUBMITTED_STR)))
            .andExpect(jsonPath("$.[*].overallRating").value(hasItem(DEFAULT_OVERALL_RATING)))
            .andExpect(jsonPath("$.[*].handwritingRating").value(hasItem(DEFAULT_HANDWRITING_RATING)))
            .andExpect(jsonPath("$.[*].usefulnessRating").value(hasItem(DEFAULT_USEFULNESS_RATING)))
            .andExpect(jsonPath("$.[*].privacy").value(hasItem(DEFAULT_PRIVACY.toString())));
    }
}
