package se.sundsvall.intricdatacollector.datasource.confluence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.intricdatacollector.datasource.confluence.JsonUtil.ANCESTOR_IDS;
import static se.sundsvall.intricdatacollector.datasource.confluence.JsonUtil.BASE_URL;
import static se.sundsvall.intricdatacollector.datasource.confluence.JsonUtil.BODY;
import static se.sundsvall.intricdatacollector.datasource.confluence.JsonUtil.CHILD_IDS;
import static se.sundsvall.intricdatacollector.datasource.confluence.JsonUtil.PATH;
import static se.sundsvall.intricdatacollector.datasource.confluence.JsonUtil.TITLE;
import static se.sundsvall.intricdatacollector.datasource.confluence.JsonUtil.UPDATED_AT;

import java.util.List;

import com.jayway.jsonpath.DocumentContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonUtilTests {

    @Nested
    class DocumentTests {

        @Mock
        private DocumentContext documentContextMock;

        @InjectMocks
        private JsonUtil.Document document;

        @Test
        void getTitle() {
            var title = "someTitle";

            when(documentContextMock.read(TITLE)).thenReturn(title);

            assertThat(document.getTitle()).isEqualTo(title);

            verify(documentContextMock).read(TITLE);
            verifyNoMoreInteractions(documentContextMock);
        }

        @Test
        void getBody() {
            var body = "someBody";

            when(documentContextMock.read(BODY)).thenReturn(body);

            assertThat(document.getBody()).isEqualTo(body);

            verify(documentContextMock).read(BODY);
            verifyNoMoreInteractions(documentContextMock);
        }

        @Test
        void getBaseUrl() {
            var baseUrl = "someBaseUrl";

            when(documentContextMock.read(BASE_URL)).thenReturn(baseUrl);

            assertThat(document.getBaseUrl()).isEqualTo(baseUrl);

            verify(documentContextMock).read(BASE_URL);
            verifyNoMoreInteractions(documentContextMock);
        }

        @Test
        void getPath() {
            var path = "somePath";

            when(documentContextMock.read(PATH)).thenReturn(path);

            assertThat(document.getPath()).isEqualTo(path);

            verify(documentContextMock).read(PATH);
            verifyNoMoreInteractions(documentContextMock);
        }

        @Test
        void getAncestorIds() {
            var ancestorIds = List.of("someAncestorId", "someOtherAncestorId");

            when(documentContextMock.read(ANCESTOR_IDS)).thenReturn(ancestorIds);

            assertThat(document.getAncestorIds()).isEqualTo(ancestorIds);

            verify(documentContextMock).read(ANCESTOR_IDS);
            verifyNoMoreInteractions(documentContextMock);
        }

        @Test
        void getChildIds() {
            var childIds = List.of("someChildId", "someOtherChildId");

            when(documentContextMock.read(CHILD_IDS)).thenReturn(childIds);

            assertThat(document.getChildIds()).isEqualTo(childIds);

            verify(documentContextMock).read(CHILD_IDS);
            verifyNoMoreInteractions(documentContextMock);
        }

        @Test
        void getUpdatedAt() {
            var updatedAt = "someUpdatedAt";

            when(documentContextMock.read(UPDATED_AT)).thenReturn(updatedAt);

            assertThat(document.getUpdatedAt()).isEqualTo(updatedAt);

            verify(documentContextMock).read(UPDATED_AT);
            verifyNoMoreInteractions(documentContextMock);
        }
    }
}
