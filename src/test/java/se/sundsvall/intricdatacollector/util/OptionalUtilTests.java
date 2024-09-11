package se.sundsvall.intricdatacollector.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static se.sundsvall.intricdatacollector.util.OptionalUtil.peek;

import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OptionalUtilTests {

    @Mock
    private Consumer<String> consumerMock;

    @Test
    void testPeek() {
        var value = "someString";

        var result = Optional.of(value)
            .map(peek(s -> consumerMock.accept(s)))
            .orElseThrow();

        // We should have the same output as the input
        assertThat(result).isEqualTo(value);

        // The consumer should have been called
        verify(consumerMock).accept(value);
    }
}
