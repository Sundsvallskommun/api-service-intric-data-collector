package se.sundsvall.intricdatacollector.datasource.confluence;

import static com.jayway.jsonpath.Option.SUPPRESS_EXCEPTIONS;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.springframework.stereotype.Component;

@Component
class JsonUtil {

    static final String TITLE           = "$.title";
    static final String BODY            = "$.body.storage.value";
    static final String BASE_URL        = "$._links.base";
    static final String PATH            = "$._links.webui";
    static final String ANCESTOR_IDS    = "$.ancestors..id";
    static final String CHILD_IDS       = "$.results..id";
    static final String UPDATED_AT      = "$.version.when";

    private final ParseContext parseContext;

    JsonUtil(final ObjectMapper objectMapper) {
        parseContext = JsonPath.using(Configuration.defaultConfiguration()
            .jsonProvider(new JacksonJsonProvider(objectMapper))
            .mappingProvider(new JacksonMappingProvider(objectMapper))
            .addOptions(SUPPRESS_EXCEPTIONS));
    }

    Document parse(final String json) {
        return new Document(parseContext.parse(json));
    }

    static class Document {

        private final DocumentContext documentContext;

        private Document(final DocumentContext documentContext) {
            this.documentContext = documentContext;
        }

        String getTitle() {
            return getValue(TITLE);
        }

        String getBody() {
            return getValue(BODY);
        }

        String getBaseUrl() {
            return getValue(BASE_URL);
        }

        String getPath() {
            return getValue(PATH);
        }

        List<String> getAncestorIds() {
            return getValues(ANCESTOR_IDS);
        }

        List<String> getChildIds() {
            return getValues(CHILD_IDS);
        }

        String getUpdatedAt() {
            return getValue(UPDATED_AT);
        }

        String getValue(final String expression) {
            return documentContext.read(expression);
        }

        List<String> getValues(final String expression) {
            return documentContext.read(expression);
        }
    }
}
