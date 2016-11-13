package com.jzheadley.noteshare.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import com.jzheadley.noteshare.domain.enumeration.Privacy;

/**
 * A Note.
 */
@Entity
@Table(name = "note")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "note")
public class Note implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Lob
    @Column(name = "note_content", nullable = false)
    private byte[] noteContent;

    @Column(name = "note_content_content_type", nullable = false)
    private String noteContentContentType;

    @NotNull
    @Column(name = "date_submitted", nullable = false)
    private ZonedDateTime dateSubmitted;

    @Column(name = "overall_rating")
    private Integer overallRating;

    @Column(name = "handwriting_rating")
    private Integer handwritingRating;

    @Column(name = "usefulness_rating")
    private Integer usefulnessRating;

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy")
    private Privacy privacy;

    @OneToOne
    @JoinColumn(unique = true)
    private User creator;

    @ManyToOne
    private Course noteCourse;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "note_tags",
               joinColumns = @JoinColumn(name="notes_id", referencedColumnName="ID"),
               inverseJoinColumns = @JoinColumn(name="tags_id", referencedColumnName="ID"))
    private Set<Tag> tags = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getNoteContent() {
        return noteContent;
    }

    public Note noteContent(byte[] noteContent) {
        this.noteContent = noteContent;
        return this;
    }

    public void setNoteContent(byte[] noteContent) {
        this.noteContent = noteContent;
    }

    public String getNoteContentContentType() {
        return noteContentContentType;
    }

    public Note noteContentContentType(String noteContentContentType) {
        this.noteContentContentType = noteContentContentType;
        return this;
    }

    public void setNoteContentContentType(String noteContentContentType) {
        this.noteContentContentType = noteContentContentType;
    }

    public ZonedDateTime getDateSubmitted() {
        return dateSubmitted;
    }

    public Note dateSubmitted(ZonedDateTime dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
        return this;
    }

    public void setDateSubmitted(ZonedDateTime dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }

    public Integer getOverallRating() {
        return overallRating;
    }

    public Note overallRating(Integer overallRating) {
        this.overallRating = overallRating;
        return this;
    }

    public void setOverallRating(Integer overallRating) {
        this.overallRating = overallRating;
    }

    public Integer getHandwritingRating() {
        return handwritingRating;
    }

    public Note handwritingRating(Integer handwritingRating) {
        this.handwritingRating = handwritingRating;
        return this;
    }

    public void setHandwritingRating(Integer handwritingRating) {
        this.handwritingRating = handwritingRating;
    }

    public Integer getUsefulnessRating() {
        return usefulnessRating;
    }

    public Note usefulnessRating(Integer usefulnessRating) {
        this.usefulnessRating = usefulnessRating;
        return this;
    }

    public void setUsefulnessRating(Integer usefulnessRating) {
        this.usefulnessRating = usefulnessRating;
    }

    public Privacy getPrivacy() {
        return privacy;
    }

    public Note privacy(Privacy privacy) {
        this.privacy = privacy;
        return this;
    }

    public void setPrivacy(Privacy privacy) {
        this.privacy = privacy;
    }

    public User getCreator() {
        return creator;
    }

    public Note creator(User user) {
        this.creator = user;
        return this;
    }

    public void setCreator(User user) {
        this.creator = user;
    }

    public Course getNoteCourse() {
        return noteCourse;
    }

    public Note noteCourse(Course course) {
        this.noteCourse = course;
        return this;
    }

    public void setNoteCourse(Course course) {
        this.noteCourse = course;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public Note tags(Set<Tag> tags) {
        this.tags = tags;
        return this;
    }

    public Note addTags(Tag tag) {
        tags.add(tag);
        return this;
    }

    public Note removeTags(Tag tag) {
        tags.remove(tag);
        return this;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Note note = (Note) o;
        if(note.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, note.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Note{" +
            "id=" + id +
            ", noteContent='" + noteContent + "'" +
            ", noteContentContentType='" + noteContentContentType + "'" +
            ", dateSubmitted='" + dateSubmitted + "'" +
            ", overallRating='" + overallRating + "'" +
            ", handwritingRating='" + handwritingRating + "'" +
            ", usefulnessRating='" + usefulnessRating + "'" +
            ", privacy='" + privacy + "'" +
            '}';
    }
}
