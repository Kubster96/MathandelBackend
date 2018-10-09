package mathandel.backend.client.model;

import com.google.gson.Gson;
import mathandel.backend.model.enums.RoleName;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTOTest {

    private String payload = "{\"Id\":1,\"name\":\"Name\",\"surname\":\"Surname\",\"username\":\"Username\",\"email\":\"useremail@email.com\",\"roles\":[{\"roleName\":\"ROLE_USER\"}]}";
    private UserTO userTO = new UserTO()
            .setId(1L)
            .setName("Name")
            .setSurname("Surname")
            .setUsername("Username")
            .setEmail("useremail@email.com")
            .setRoles(Collections.singleton(new RoleTO().setRoleName(RoleName.ROLE_USER)));
    private Gson gson = new Gson();


    @Test
    public void shouldMarshalUserTO() {
        //when
        String actual = gson.toJson(userTO);

        //then
        assertThat(actual).isEqualTo(payload);
    }

    @Test
    public void shouldUnmarshalUserTO() {
        //when
        UserTO actual = gson.fromJson(payload, UserTO.class);

        //then
        assertThat(actual).isEqualTo(userTO);
    }
}