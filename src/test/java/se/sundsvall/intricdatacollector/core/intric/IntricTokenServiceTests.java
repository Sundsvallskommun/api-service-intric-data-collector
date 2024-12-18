package se.sundsvall.intricdatacollector.core.intric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class IntricTokenServiceTests {

	@Mock
	private RestClient restClientMock;
	@Mock
	private RestClient.RequestBodyUriSpec requestBodyUriSpecMock;
	@Mock
	private RestClient.RequestBodySpec requestBodySpecMock;
	@Mock
	private RestClient.ResponseSpec responseSpecMock;

	@Mock
	private ResponseEntity<IntricTokenService.AccessTokenResponse> responseEntityMock;

	private IntricTokenService tokenService;

	@BeforeEach
	void setUp() {
		tokenService = new IntricTokenService(restClientMock, "someUsername", "somePassword");
	}

	@Test
	void getToken() {
		// Create a (non-expired) token
		var accessToken = createToken(Instant.now().plus(1, ChronoUnit.DAYS));

		when(responseEntityMock.getBody()).thenReturn(new IntricTokenService.AccessTokenResponse(accessToken, "bearer"));
		when(restClientMock.post()).thenReturn(requestBodyUriSpecMock);
		when(requestBodyUriSpecMock.body(any(MultiValueMap.class))).thenReturn(requestBodySpecMock);
		when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);
		when(responseSpecMock.toEntity(IntricTokenService.AccessTokenResponse.class)).thenReturn(responseEntityMock);

		// Do a token request
		tokenService.getToken();
		// Verify that a single POST request was made
		verify(restClientMock).post();
		verify(requestBodyUriSpecMock).body(any(MultiValueMap.class));
		verify(requestBodySpecMock).retrieve();
		verify(responseSpecMock).toEntity(IntricTokenService.AccessTokenResponse.class);
		verify(responseEntityMock).getBody();

		// Do another token request
		tokenService.getToken();

		// Verify that no additional POST request was made, since the token expires a week from now
		verifyNoMoreInteractions(responseEntityMock, restClientMock, requestBodyUriSpecMock, requestBodySpecMock, responseSpecMock);
	}

	@Test
	void getTokenWhenTokenIsExpired() {
		// Create an expired token
		var accessToken = createToken(Instant.now().minus(1, ChronoUnit.HOURS));

		when(responseEntityMock.getBody()).thenReturn(new IntricTokenService.AccessTokenResponse(accessToken, "bearer"));
		when(restClientMock.post()).thenReturn(requestBodyUriSpecMock);
		when(requestBodyUriSpecMock.body(any(MultiValueMap.class))).thenReturn(requestBodySpecMock);
		when(requestBodySpecMock.retrieve()).thenReturn(responseSpecMock);
		when(responseSpecMock.toEntity(IntricTokenService.AccessTokenResponse.class)).thenReturn(responseEntityMock);

		// Do a token request
		tokenService.getToken();
		// Do another token request
		tokenService.getToken();

		// Verify that two POST requests were made
		verify(restClientMock, times(2)).post();
		verify(requestBodyUriSpecMock, times(2)).body(any(MultiValueMap.class));
		verify(requestBodySpecMock, times(2)).retrieve();
		verify(responseSpecMock, times(2)).toEntity(IntricTokenService.AccessTokenResponse.class);
		verify(responseEntityMock, times(2)).getBody();

		verifyNoMoreInteractions(responseEntityMock, restClientMock, requestBodyUriSpecMock, requestBodySpecMock, responseSpecMock);
	}

	// Instant.now().minus(1, ChronoUnit.MINUTES)
	private String createToken(final Instant expiresAt) {
		return JWT.create()
			.withSubject("someSubject")
			.withIssuer("someIssuer")
			.withAudience("test")
			.withIssuedAt(Instant.now().minus(1, ChronoUnit.DAYS))
			.withExpiresAt(expiresAt)
			.sign(Algorithm.HMAC256("p4ssw0rd"));
	}

	@Test
	void accessTokenResponseCreationAndAccessors() {
		var accessToken = "someAccessToken";
		var tokenType = "someTokenType";

		var accessTokenResponse = new IntricTokenService.AccessTokenResponse(accessToken, tokenType);

		assertThat(accessTokenResponse.accessToken()).isEqualTo(accessToken);
		assertThat(accessTokenResponse.tokenType()).isEqualTo(tokenType);
	}
}
