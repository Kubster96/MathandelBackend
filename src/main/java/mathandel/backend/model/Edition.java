package mathandel.backend.model;

import org.hibernate.annotations.NaturalId;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "editions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "name"
        })
})
public class Edition {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @Size(max = 40)
    private String name;

    private LocalDate endDate;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "moderators",
            joinColumns = @JoinColumn(name = "edition_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> moderators = new HashSet<>();

    public Edition() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<User> getModerators() {
        return moderators;
    }

    public void setModerators(Set<User> moderators) {
        this.moderators = moderators;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
