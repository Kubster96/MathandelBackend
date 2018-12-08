package mathandel.backend.model.server;

import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "preferences")
public class Preference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @NaturalId
    @OneToOne
    private Item haveItem;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Item> wantedItems = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<DefinedGroup> wantedDefinedGroups = new HashSet<>();

    @ManyToOne
    private Edition edition;

    public Set<DefinedGroup> getWantedDefinedGroups() {
        return wantedDefinedGroups;
    }

    public Set<Item> getWantedItems() {
        return wantedItems;
    }

    public Preference setWantedItems(Set<Item> wantedItems) {
        this.wantedItems = wantedItems;
        return this;
    }

    public Preference setWantedDefinedGroups(Set<DefinedGroup> wantedDefinedGroups) {
        this.wantedDefinedGroups = wantedDefinedGroups;
        return this;
    }

    public Long getId() {
        return id;
    }

    public Preference setId(Long id) {
        this.id = id;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Preference setUser(User user) {
        this.user = user;
        return this;
    }

    public Item getHaveItem() {
        return haveItem;
    }

    public Preference setHaveItem(Item haveItem) {
        this.haveItem = haveItem;
        return this;
    }

    public Edition getEdition() {
        return edition;
    }

    public Preference setEdition(Edition edition) {
        this.edition = edition;
        return this;
    }
}

