package mathandel.backend.client.request;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PasswordRequest {
    private String newPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public PasswordRequest setNewPassword(String newPassword) {
        this.newPassword = newPassword;
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

        PasswordRequest that = (PasswordRequest) o;

        return new EqualsBuilder()
                .append(newPassword, that.newPassword)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(newPassword)
                .toHashCode();
    }
}
