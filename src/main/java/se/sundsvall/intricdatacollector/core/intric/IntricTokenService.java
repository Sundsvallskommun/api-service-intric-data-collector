package se.sundsvall.intricdatacollector.core.intric;

import static java.time.Instant.now;
import static java.util.Optional.ofNullable;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import java.time.Instant;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.zalando.problem.Problem;

class IntricTokenService {

    private final MultiValueMap<String, String> accessTokenRequestData;
    private final RestClient restClient;

    private Instant tokenExpiration;
    private String token;

    IntricTokenService(final RestClient restClient, final String username, final String password) {
        this.restClient = restClient;

        accessTokenRequestData = new LinkedMultiValueMap<>();
        accessTokenRequestData.add("grant_type", "");
        accessTokenRequestData.add("username", username);
        accessTokenRequestData.add("password", password);
        accessTokenRequestData.add("scope", "");
        accessTokenRequestData.add("client_id", "");
        accessTokenRequestData.add("client_secret", "");
    }

    String getToken() {
        // If we don't have a token at all, or if it's expired - get a new one
        if (token == null || (tokenExpiration != null && tokenExpiration.isBefore(now()))) {
            var tokenResponse = retrieveToken();

            token = ofNullable(tokenResponse.getBody())
                .map(AccessTokenResponse::accessToken)
                .orElseThrow(() -> Problem.valueOf(INTERNAL_SERVER_ERROR, "Unable to extract access token"));

            // Decode the token to extract the expiresAt instant
            var jwt = JWT.decode(token);
            tokenExpiration = jwt.getExpiresAtAsInstant();
        }

        return token;
    }

    ResponseEntity<AccessTokenResponse> retrieveToken() {
        return restClient.post()
            .body(accessTokenRequestData)
            .retrieve()
            .toEntity(AccessTokenResponse.class);
    }

    record AccessTokenResponse(

        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("token_type")
        String tokenType) { }
}
