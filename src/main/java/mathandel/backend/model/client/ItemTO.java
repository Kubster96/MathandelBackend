package mathandel.backend.model.client;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

public class ItemTO {

    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    private Long userId;
    private Long editionId;
    @NotNull
    private Set<ImageTO> images;

    public Long getId() {
        return id;
    }

    public ItemTO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ItemTO setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ItemTO setDescription(String description) {
        this.description = description;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public ItemTO setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public Set<ImageTO> getImages() {
        return images;
    }

    public ItemTO setImages(Set<ImageTO> images) {
        this.images = images;
        return this;
    }

    public Long getEditionId() {
        return editionId;
    }

    public ItemTO setEditionId(Long editionId) {
        this.editionId = editionId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ItemTO item = (ItemTO) o;

        return new EqualsBuilder()
                .append(id, item.id)
                .append(name, item.name)
                .append(description, item.description)
                .append(userId, item.userId)
                .append(editionId, item.editionId)
                .append(images, item.images)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(description)
                .append(userId)
                .append(editionId)
                .append(images)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("description", description)
                .append("userId", userId)
                .append("editionId", editionId)
                .append("images", images)
                .toString();
    }
}
