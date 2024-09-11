package se.sundsvall.intricdatacollector.datasource.confluence.integration.confluence;

import static java.util.Optional.ofNullable;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ProblemDetail;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
class WebhookSignatureVerificationConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(WebhookSignatureVerificationConfiguration.class);

    static final String SIGNATURE_HEADER = "x-hub-signature";
    static final String SIGNATURE_PREFIX = "sha256=";

    @Bean
    @ConditionalOnProperty(name = "integration.confluence.webhook-security.enabled", havingValue = "true", matchIfMissing = true)
    FilterRegistrationBean<WebhookSignatureVerificationFilter> webhookSignatureVerificationFilter(final ConfluenceIntegrationProperties properties) {
        LOG.info("Adding webhook signature verification filter");

        var filterRegistration = new FilterRegistrationBean<>(
            new WebhookSignatureVerificationFilter(properties.webhookSecurity().secret()));
        filterRegistration.setUrlPatterns(List.of("/confluence/webhook-event"));
        return filterRegistration;
    }

    static class WebhookSignatureVerificationFilter extends OncePerRequestFilter {

        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(SerializationFeature.INDENT_OUTPUT, false);

        private final HmacUtils hmacUtils;

        WebhookSignatureVerificationFilter(final String secret) {
            hmacUtils = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secret);
        }

        @Override
        protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws ServletException, IOException {
            var requestWrapper = new BodyCachingHttpServletRequestWrapper(request);
            var body = new String(requestWrapper.cachedBody, StandardCharsets.UTF_8);
            var minifiedBody = OBJECT_MAPPER.readTree(body).toString();
            var headerSignature = ofNullable(requestWrapper.getHeader(SIGNATURE_HEADER))
                .map(s -> s.replace(SIGNATURE_PREFIX, ""))
                .orElse("");
            var bodySignature = hmacUtils.hmacHex(minifiedBody);

            if (!bodySignature.equals(headerSignature)) {
                var problem = ProblemDetail.forStatus(FORBIDDEN);
                problem.setDetail("Webhook signature verification failed");

                response.setStatus(FORBIDDEN.value());
                response.setHeader(CONTENT_TYPE, APPLICATION_PROBLEM_JSON_VALUE);
                response.getWriter().println(OBJECT_MAPPER.writeValueAsString(problem));
                response.flushBuffer();
            } else {
                LOG.debug("Webhook signature verified");

                chain.doFilter(requestWrapper, response);
            }
        }

        static class BodyCachingHttpServletRequestWrapper extends HttpServletRequestWrapper {

            private final byte[] cachedBody;

            BodyCachingHttpServletRequestWrapper(final HttpServletRequest request) throws IOException {
                super(request);

                cachedBody = toByteArray(request.getInputStream());
            }

            @Override
            public ServletInputStream getInputStream() {
                return new BodyCachingServletInputStream(cachedBody);
            }

            @Override
            public BufferedReader getReader() {
                return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cachedBody)));
            }
        }

        static class BodyCachingServletInputStream extends ServletInputStream {

            private final InputStream cachedInputStream;

            BodyCachingServletInputStream(final byte[] bodyToCache) {
                cachedInputStream = new ByteArrayInputStream(bodyToCache);
            }

            @Override
            public boolean isFinished() {
                try {
                    return cachedInputStream.available() == 0;
                } catch (IOException e) {
                    return false;
                }
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(final ReadListener readListener) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int read() throws IOException {
                return cachedInputStream.read();
            }
        }
    }
}
