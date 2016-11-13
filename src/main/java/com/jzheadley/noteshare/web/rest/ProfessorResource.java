package com.jzheadley.noteshare.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.jzheadley.noteshare.domain.Professor;

import com.jzheadley.noteshare.repository.ProfessorRepository;
import com.jzheadley.noteshare.repository.search.ProfessorSearchRepository;
import com.jzheadley.noteshare.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Professor.
 */
@RestController
@RequestMapping("/api")
public class ProfessorResource {

    private final Logger log = LoggerFactory.getLogger(ProfessorResource.class);
        
    @Inject
    private ProfessorRepository professorRepository;

    @Inject
    private ProfessorSearchRepository professorSearchRepository;

    /**
     * POST  /professors : Create a new professor.
     *
     * @param professor the professor to create
     * @return the ResponseEntity with status 201 (Created) and with body the new professor, or with status 400 (Bad Request) if the professor has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/professors")
    @Timed
    public ResponseEntity<Professor> createProfessor(@Valid @RequestBody Professor professor) throws URISyntaxException {
        log.debug("REST request to save Professor : {}", professor);
        if (professor.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("professor", "idexists", "A new professor cannot already have an ID")).body(null);
        }
        Professor result = professorRepository.save(professor);
        professorSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/professors/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("professor", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /professors : Updates an existing professor.
     *
     * @param professor the professor to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated professor,
     * or with status 400 (Bad Request) if the professor is not valid,
     * or with status 500 (Internal Server Error) if the professor couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/professors")
    @Timed
    public ResponseEntity<Professor> updateProfessor(@Valid @RequestBody Professor professor) throws URISyntaxException {
        log.debug("REST request to update Professor : {}", professor);
        if (professor.getId() == null) {
            return createProfessor(professor);
        }
        Professor result = professorRepository.save(professor);
        professorSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("professor", professor.getId().toString()))
            .body(result);
    }

    /**
     * GET  /professors : get all the professors.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of professors in body
     */
    @GetMapping("/professors")
    @Timed
    public List<Professor> getAllProfessors() {
        log.debug("REST request to get all Professors");
        List<Professor> professors = professorRepository.findAll();
        return professors;
    }

    /**
     * GET  /professors/:id : get the "id" professor.
     *
     * @param id the id of the professor to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the professor, or with status 404 (Not Found)
     */
    @GetMapping("/professors/{id}")
    @Timed
    public ResponseEntity<Professor> getProfessor(@PathVariable Long id) {
        log.debug("REST request to get Professor : {}", id);
        Professor professor = professorRepository.findOne(id);
        return Optional.ofNullable(professor)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /professors/:id : delete the "id" professor.
     *
     * @param id the id of the professor to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/professors/{id}")
    @Timed
    public ResponseEntity<Void> deleteProfessor(@PathVariable Long id) {
        log.debug("REST request to delete Professor : {}", id);
        professorRepository.delete(id);
        professorSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("professor", id.toString())).build();
    }

    /**
     * SEARCH  /_search/professors?query=:query : search for the professor corresponding
     * to the query.
     *
     * @param query the query of the professor search 
     * @return the result of the search
     */
    @GetMapping("/_search/professors")
    @Timed
    public List<Professor> searchProfessors(@RequestParam String query) {
        log.debug("REST request to search Professors for query {}", query);
        return StreamSupport
            .stream(professorSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }


}
