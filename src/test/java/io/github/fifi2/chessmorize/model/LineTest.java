package io.github.fifi2.chessmorize.model;

import io.github.fifi2.chessmorize.model.Line;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LineTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        2024-03-22T22:46:56Z |                      | true
        2024-03-22T22:47:56Z | 2024-03-21T20:00:00Z | true
        2024-03-22T22:47:56Z | 2024-03-22T00:00:00Z | false
        2024-03-22T22:48:56Z | 2024-03-22T22:40:00Z | false
        """)
    void hasNotBeenTrainedToday(final String givenNow,
                                final String lastTraining,
                                final boolean expectedResult) {

        final Instant fakeNow = Instant.now(Clock.fixed(
            Instant.parse(givenNow),
            ZoneId.of("UTC")));

        final Line line = Line.builder()
            .lastTraining(Optional
                .ofNullable(lastTraining)
                .map(Instant::parse)
                .orElse(null))
            .build();

        try (MockedStatic<Instant> instantMock =
                 Mockito.mockStatic(Instant.class)) {
            instantMock.when(Instant::now).thenReturn(fakeNow);
            assertThat(line.hasNotBeenTrainedToday()).isEqualTo(expectedResult);
        }
    }

}
