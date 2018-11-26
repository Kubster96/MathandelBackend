package mathandel.backend.model.server;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "items")
public class Item {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Id
    @GenericGenerator(name = "UseExistingIdOtherwiseGenerateUsingIdentity", strategy = "mathandel.backend.model.generator.UseExistingIdOtherwiseGenerateUsingIdentity")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "UseExistingIdOtherwiseGenerateUsingIdentity")
    private Long id;

    private String name;

    private String description;

    @ManyToOne
    private User user;

    @ManyToOne
    private Edition edition;

    @OneToMany(cascade = {CascadeType.ALL})
    private Set<Image> images = new HashSet<>();

    public Item() {
    }

    public Long getId() {
        return id;
    }

    public Item setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Item setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Item setDescription(String description) {
        this.description = description;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Item setUser(User user) {
        this.user = user;
        return this;
    }

    public Edition getEdition() {
        return edition;
    }

    public Item setEdition(Edition edition) {
        this.edition = edition;
        return this;
    }

    public Set<Image> getImages() {
        return images;
    }

    public Item setImages(Set<Image> images) {
        this.images = images;
        return this;
    }
}
