package com.jzheadley.noteshare.repository;

import com.jzheadley.noteshare.domain.Note;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Note entity.
 */
@SuppressWarnings("unused")
public interface NoteRepository extends JpaRepository<Note,Long> {

    @Query("select distinct note from Note note left join fetch note.tags")
    List<Note> findAllWithEagerRelationships();

    @Query("select note from Note note left join fetch note.tags where note.id =:id")
    Note findOneWithEagerRelationships(@Param("id") Long id);

}
