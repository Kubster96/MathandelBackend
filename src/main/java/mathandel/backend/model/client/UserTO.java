package mathandel.backend.model.client;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

public class UserTO {

    private Long id;

    @NotBlank
    @Size(min = 4, max = 40)
    private String name;

    @NotBlank
    @Size(min = 4, max = 40)
    private String surname;

    @NotBlank
    @Size(min = 3, max = 15)
    private String username;

    @NotBlank
    @Size(max = 40)
    @Email
    private String email;

    private Set<RoleTO> roles;

    public Long getId() {
        return id;
    }

    public UserTO setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserTO setName(String name) {
        this.name = name;
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public UserTO setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UserTO setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserTO setEmail(String email) {
        this.email = email;
        return this;
    }

    public Set<RoleTO> getRoles() {
        return roles;
    }

    public UserTO setRoles(Set<RoleTO> roles) {
        this.roles = roles;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserTO userTO = (UserTO) o;

        return new EqualsBuilder()
                .append(id, userTO.id)
                .append(name, userTO.name)
                .append(surname, userTO.surname)
                .append(username, userTO.username)
                .append(email, userTO.email)
                .append(roles, userTO.roles)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(name)
                .append(surname)
                .append(username)
                .append(email)
                .append(roles)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("surname", surname)
                .append("username", username)
                .append("email", email)
                .append("roles", roles)
                .toString();
    }
}
