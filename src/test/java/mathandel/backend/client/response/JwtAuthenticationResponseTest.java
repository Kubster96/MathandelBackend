package mathandel.backend.client.response;

import com.google.gson.Gson;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtAuthenticationResponseTest {

    private String payload = "{\"accessToken\":\"token\",\"tokenType\":\"Bearer\"}";
    private JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse("token");
    private Gson gson = new Gson();

    @Test
    public void shouldMarshalJwtAuthenticationResponse() {
        //when
        String actual = gson.toJson(jwtAuthenticationResponse);

        //then
        assertThat(actual).isEqualTo(payload);
    }

    @Test
    public void shouldUnmarshalJwtAuthenticationResponse() {
        //when
        JwtAuthenticationResponse actual = gson.fromJson(payload, JwtAuthenticationResponse.class);

        //then
        assertThat(actual).isEqualTo(jwtAuthenticationResponse);
    }
}