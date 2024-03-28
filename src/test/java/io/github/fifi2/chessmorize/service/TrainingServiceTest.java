package io.github.fifi2.chessmorize.service;

import io.github.fifi2.chessmorize.config.properties.TrainingProperties;
import io.github.fifi2.chessmorize.error.exception.BookNotFoundException;
import io.github.fifi2.chessmorize.error.exception.LineNotFoundException;
import io.github.fifi2.chessmorize.error.exception.NoTrainingLineException;
import io.github.fifi2.chessmorize.helper.converter.StringToList;
import io.github.fifi2.chessmorize.model.Book;
import io.github.fifi2.chessmorize.model.Line;
import io.github.fifi2.chessmorize.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private TrainingProperties trainingProperties;

    @InjectMocks
    private TrainingService trainingService;

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void getNextLine(final int eligibleLinesNumber) {

        final UUID bookId = UUID.randomUUID();

        final List<Line> lines = new ArrayList<>();
        for (int i = 0; i < eligibleLinesNumber; i++) {
            lines.add(Line.builder()
                .boxId(0)
                .build());
        }

        Mockito
            .when(this.bookRepository.findById(bookId))
            .thenReturn(Mono.just(Book.builder()
                .calendarSlot(0)
                .lines(lines)
                .build()));

        Mockito
            .when(this.trainingProperties.getCalendar())
            .thenReturn(List.of(List.of(0)));

        final StepVerifier.FirstStep<Line> nextLineStepVerifier = StepVerifier
            .create(this.trainingService.getNextLine(bookId));

        if (eligibleLinesNumber > 0) {
            nextLineStepVerifier
                .expectNextMatches(line -> line.getBoxId() == 0)
                .verifyComplete();
        } else {
            nextLineStepVerifier
                .expectError(NoTrainingLineException.class)
                .verify();
        }
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        0 |   |                             | with no lines #1
        1 |   |                             | with no lines #2
        0 | A | A;0;null;null               | with 1 possible line
        0 | A | A;0;null;null,B;0;null;null | priority to the 1st possible line
        1 | B | A;1;null;null,B;0;null;null | priority to the lowest box
        2 | B | A;1;null;null,B;0;null;null | priority to the session box
        2 |   | A;1;null;null,B;1;null;null | no line in the session box #1
        0 |   | A;1;null;null               | no line in the session box #2
        2 | B | A;0;0;2,B;2;1440;1          | do not loop on the same line #1
        2 | B | A;0;2;2,B;2;10;1            | do not loop on the same line #2
        """)
    void pickNextLine(
        final int currentCalendarSlot,
        final String expectedLineName,
        @ConvertWith(StringToList.class) final List<String> linesSpec) {

        final String specSplitter = ";";
        final Instant now = Instant.now();

        Mockito
            .when(this.trainingProperties.getCalendar())
            .thenReturn(List.of(
                List.of(0),
                List.of(0, 1),
                List.of(0, 2),
                List.of(0, 1)));

        final Map<String, Line> linesByName = linesSpec
            .stream()
            .collect(Collectors.toMap(
                lineSpec -> lineSpec.split(specSplitter)[0],
                lineSpec -> {
                    final String[] s = lineSpec.split(specSplitter);
                    final Duration lastTrainingOffset = "null".equals(s[2])
                        ? Duration.of(0, ChronoUnit.MINUTES)
                        : Duration.of(Integer.parseInt(s[2]), ChronoUnit.MINUTES);
                    return Line.builder()
                        .boxId(Integer.parseInt(s[1]))
                        .lastTraining(now.minus(lastTrainingOffset))
                        .lastCalendarSlot("null".equals(s[3])
                            ? null
                            : Integer.parseInt(s[3]))
                        .build();
                }
            ));

        final Optional<Line> line = this.trainingService
            .pickNextLine(Book.builder()
                .calendarSlot(currentCalendarSlot)
                .lines(linesByName.values().stream().toList())
                .build());

        Optional
            .ofNullable(expectedLineName)
            .ifPresentOrElse(
                name -> assertThat(line).hasValue(linesByName.get(name)),
                () -> assertThat(line).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void setLineResult(final boolean result) {

        final Instant start = Instant.now();
        final UUID bookId = UUID.randomUUID();
        final UUID lineId = UUID.randomUUID();
        final Book book = Book.builder()
            .id(bookId)
            .lines(List.of(Line.builder()
                .id(lineId)
                .build()))
            .build();

        Mockito
            .when(this.bookRepository.findById(book.getId()))
            .thenReturn(Mono.just(book));

        if (result) {
            Mockito
                .when(this.trainingProperties.getMaxNumber())
                .thenReturn(2);
        }

        Mockito
            .when(this.bookRepository.update(book))
            .thenReturn(Mono.just(book));

        StepVerifier
            .create(this.trainingService.setLineResult(bookId,
                lineId,
                result))
            .expectNextMatches(line -> line.getBoxId() == (result ? 1 : 0)
                && line.getLastTraining() != null
                && line.getLastTraining().compareTo(start) >= 0
                && line.getLastTraining().compareTo(Instant.now()) <= 0
                && line.getLastCalendarSlot() == 0)
            .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void setLineResult_withBookNotFound(final boolean result) {

        final UUID bookId = UUID.randomUUID();

        Mockito
            .when(this.bookRepository.findById(bookId))
            .thenReturn(Mono.error(new BookNotFoundException(bookId)));

        StepVerifier
            .create(this.trainingService.setLineResult(bookId,
                UUID.randomUUID(),
                result))
            .expectError(BookNotFoundException.class)
            .verify();

        Mockito
            .verify(this.bookRepository, Mockito.never())
            .update(Mockito.any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void setLineResult_withLineNotFound(final boolean result) {

        final UUID bookId = UUID.randomUUID();
        final Book book = Book.builder()
            .id(bookId)
            .lines(List.of())
            .build();

        Mockito
            .when(this.bookRepository.findById(book.getId()))
            .thenReturn(Mono.just(book));

        StepVerifier
            .create(this.trainingService.setLineResult(bookId,
                UUID.randomUUID(),
                result))
            .expectError(LineNotFoundException.class)
            .verify();

        Mockito
            .verify(this.bookRepository, Mockito.never())
            .update(Mockito.any());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        0 | 1 | 0
        0 | 2 | 1
        0 | 3 | 1
        1 | 2 | 0
        2 | 3 | 0
        """)
    void nextCalendarSlot(final int initialCalendarSlot,
                          final int calendarSize,
                          final int expectedCalendarSlot) {

        final UUID bookId = UUID.randomUUID();
        final Book book = Book.builder()
            .id(bookId)
            .lines(List.of())
            .calendarSlot(initialCalendarSlot)
            .build();

        Mockito
            .when(this.bookRepository.findById(bookId))
            .thenReturn(Mono.just(book));

        final List<List<Integer>> calendar = new ArrayList<>();
        for (int i = 0; i < calendarSize; i++)
            calendar.add(List.of());
        Mockito
            .when(this.trainingProperties.getCalendar())
            .thenReturn(calendar);

        Mockito
            .when(this.bookRepository.update(book))
            .thenReturn(Mono.just(book));

        StepVerifier
            .create(this.trainingService.nextCalendarSlot(bookId))
            .expectNextMatches(b -> bookId.equals(b.getId())
                && b.getCalendarSlot() == expectedCalendarSlot)
            .verifyComplete();
    }

    @Test
    void nextCalendarSlot_withNoBookFound() {

        Mockito
            .when(this.bookRepository.findById(Mockito.any()))
            .thenReturn(Mono.empty());

        StepVerifier
            .create(this.trainingService.nextCalendarSlot(UUID.randomUUID()))
            .verifyComplete();

        Mockito
            .verify(this.bookRepository, Mockito.never())
            .update(Mockito.any());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        0 | 0 | 0
        0 | 1 | 1
        0 | 2 | 1
        1 | 1 | 1
        1 | 2 | 2
        0 | 5 | 1
        1 | 5 | 2
        4 | 5 | 5
        5 | 5 | 5
        """)
    void computeNextBoxId(final int initialBoxId,
                          final int boxNumber,
                          final int expectedNewBoxId) {

        final Line line = Line.builder()
            .boxId(initialBoxId)
            .build();

        Mockito
            .when(this.trainingProperties.getMaxNumber())
            .thenReturn(boxNumber);

        assertThat(this.trainingService.computeNextBoxId(line))
            .isEqualTo(expectedNewBoxId);
    }

}
