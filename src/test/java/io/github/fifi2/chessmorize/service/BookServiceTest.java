package io.github.fifi2.chessmorize.service;

import io.github.fifi2.chessmorize.api.LichessApiClient;
import io.github.fifi2.chessmorize.config.properties.TrainingProperties;
import io.github.fifi2.chessmorize.converter.PgnGamesToBookConverter;
import io.github.fifi2.chessmorize.error.exception.pgn.PgnException;
import io.github.fifi2.chessmorize.helper.builder.BookBuilder;
import io.github.fifi2.chessmorize.model.*;
import io.github.fifi2.chessmorize.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    private static final String STUDY_ID = "whatever";
    private static final UUID BOOK_ID = UUID.randomUUID();

    @Mock
    private LichessApiClient lichessApiClientMock;

    @Mock
    private PgnGamesToBookConverter pgnGamesToBookConverterMock;

    @Mock
    private BookRepository bookRepositoryMock;

    @Mock
    private TrainingProperties trainingPropertiesMock;

    @InjectMocks
    private BookService bookService;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void createBook(final boolean isShuffled) {

        Mockito
            .when(this.lichessApiClientMock.getStudyPGN(STUDY_ID))
            .thenReturn(Mono.just("1. d4 d5 *"));

        Mockito
            .when(this.pgnGamesToBookConverterMock.convert(
                Mockito.anyList(),
                Mockito.eq(STUDY_ID),
                Mockito.eq(Color.WHITE)))
            .thenReturn(Book.builder()
                .studyId(STUDY_ID)
                .color(Color.WHITE)
                .chapters(List.of())
                .build());

        Mockito
            .when(this.bookRepositoryMock.save(Mockito.any()))
            .thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0)));

        Mockito
            .when(this.trainingPropertiesMock.isShuffled())
            .thenReturn(isShuffled);

        StepVerifier
            .create(this.bookService.createBook(STUDY_ID, Color.WHITE))
            .expectNextMatches(book -> STUDY_ID.equals(book.getStudyId())
                && Color.WHITE.equals(book.getColor()))
            .verifyComplete();

        Mockito
            .verify(
                this.bookRepositoryMock,
                Mockito.times(1))
            .save(Mockito.any());
    }

    @Test
    void createBook_withInvalidPgn() {

        Mockito
            .when(this.lichessApiClientMock.getStudyPGN(STUDY_ID))
            .thenReturn(Mono.just("""
                [Event "invalid pgn"]
                
                1. Da1 *
                """));

        StepVerifier
            .create(this.bookService.createBook(STUDY_ID, Color.WHITE))
            .expectError(PgnException.class)
            .verify();

        Mockito
            .verify(this.pgnGamesToBookConverterMock, Mockito.never())
            .convert(Mockito.anyList(), Mockito.anyString(), Mockito.any());

        Mockito
            .verify(this.bookRepositoryMock, Mockito.never())
            .save(Mockito.any());
    }

    @Test
    void getOneBook() {

        Mockito
            .when(this.bookRepositoryMock.findById(BOOK_ID))
            .thenReturn(Mono.just(Book.builder().id(BOOK_ID).build()));

        StepVerifier
            .create(this.bookService.getOneBook(BOOK_ID))
            .expectNextMatches(book -> BOOK_ID.equals(book.getId()))
            .verifyComplete();

        Mockito
            .verify(this.bookRepositoryMock, Mockito.only())
            .findById(BOOK_ID);
    }

    @Test
    void getAllBooks() {

        final UUID bookId1 = UUID.randomUUID();
        final UUID bookId2 = UUID.randomUUID();

        Mockito
            .when(this.bookRepositoryMock.findAll())
            .thenReturn(Flux.just(
                Book.builder().id(bookId1).build(),
                Book.builder().id(bookId2).build()));

        StepVerifier
            .create(this.bookService.getAllBooks())
            .expectNextMatches(book -> bookId1.equals(book.getId()))
            .expectNextMatches(book -> bookId2.equals(book.getId()))
            .verifyComplete();

        Mockito
            .verify(this.bookRepositoryMock, Mockito.only())
            .findAll();
    }

    @Test
    void deleteOneBook() {

        Mockito
            .when(this.bookRepositoryMock.deleteById(BOOK_ID))
            .thenReturn(Mono.just(BOOK_ID));

        StepVerifier
            .create(this.bookService.deleteOneBook(BOOK_ID))
            .expectNextMatches(BOOK_ID::equals)
            .verifyComplete();

        Mockito
            .verify(this.bookRepositoryMock, Mockito.only())
            .deleteById(BOOK_ID);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void createLines(final boolean isShuffled) {

        final UUID chapterId1 = UUID.randomUUID();
        final UUID chapterId2 = UUID.randomUUID();
        final UUID id1 = UUID.randomUUID();
        final UUID id2 = UUID.randomUUID();
        final UUID id3 = UUID.randomUUID();
        final UUID id11 = UUID.randomUUID();
        final UUID id12 = UUID.randomUUID();
        final UUID id31 = UUID.randomUUID();

        final Book book = Book.builder()
            .chapters(List.of(
                Chapter.builder()
                    .id(chapterId1)
                    .nextMoves(List.of(
                        Move.builder()
                            .id(id1)
                            .nextMoves(List.of(
                                Move.builder()
                                    .id(id11)
                                    .build(),
                                Move.builder()
                                    .id(id12)
                                    .build()
                            ))
                            .build(),
                        Move.builder()
                            .id(id2)
                            .build()))
                    .build(),
                Chapter.builder()
                    .id(chapterId2)
                    .nextMoves(List.of(
                        Move.builder()
                            .id(id3)
                            .nextMoves(List.of(
                                Move.builder()
                                    .id(id31)
                                    .build()
                            ))
                            .build()))
                    .build()))
            .build();

        Mockito
            .when(this.trainingPropertiesMock.isShuffled())
            .thenReturn(isShuffled);

        final List<Line> lines = this.bookService.createLines(book);

        assertThat(lines)
            .hasSize(4)
            .extracting(Line::getChapterId)
            .containsExactlyInAnyOrder(
                chapterId1,
                chapterId1,
                chapterId1,
                chapterId2);

        assertThat(lines)
            .extracting(Line::getBoxId)
            .containsOnly(0);

        Map<UUID, List<List<UUID>>> linesIdsByChapterId = lines
            .stream()
            .collect(Collectors.groupingBy(
                Line::getChapterId,
                Collectors.mapping(
                    line -> line.getMoves()
                        .stream()
                        .map(LineMove::getMoveId)
                        .toList(),
                    Collectors.toList())));

        assertThat(linesIdsByChapterId.get(chapterId1))
            .containsExactlyInAnyOrder(
                List.of(id1, id11),
                List.of(id1, id12),
                List.of(id2));

        assertThat(linesIdsByChapterId.get(chapterId2))
            .containsExactlyInAnyOrder(List.of(id3, id31));
    }

    @Test
    void buildChapterLines() {

        final UUID id1 = UUID.randomUUID();
        final UUID id11 = UUID.randomUUID();
        final UUID id12 = UUID.randomUUID();
        final UUID id111 = UUID.randomUUID();
        final UUID id112 = UUID.randomUUID();
        final UUID id1121 = UUID.randomUUID();
        final UUID id113 = UUID.randomUUID();
        final UUID id121 = UUID.randomUUID();
        final UUID id1111 = UUID.randomUUID();
        final UUID id13 = UUID.randomUUID();
        final UUID id131 = UUID.randomUUID();

        final Move move = Move.builder()
            .id(id1)
            .color(Color.BLACK)
            .comment("comment")
            .nextMoves(List.of(
                Move.builder()
                    .id(id11)
                    .color(Color.WHITE)
                    .nextMoves(List.of(
                        // A mistake an adversary could play, to inline.
                        Move.builder()
                            .id(id111)
                            .color(Color.BLACK)
                            .nag(Nag.BLUNDER)
                            .nextMoves(List.of(
                                Move.builder()
                                    .id(id1111)
                                    .color(Color.WHITE)
                                    .build()))
                            .build(),
                        Move.builder()
                            .id(id112)
                            .color(Color.BLACK)
                            .nextMoves(List.of(Move.builder()
                                .id(id1121)
                                .color(Color.WHITE)
                                .build()))
                            .build(),
                        Move.builder()
                            .id(id113)
                            .color(Color.BLACK)
                            .build()))
                    .build(),
                Move.builder()
                    .id(id12)
                    .color(Color.WHITE)
                    .nextMoves(List.of(
                        Move.builder()
                            .id(id121)
                            .color(Color.BLACK)
                            .build()))
                    .build(),
                // A mistake for educational purposes, not to inline.
                Move.builder()
                    .id(id13)
                    .color(Color.WHITE)
                    .nag(Nag.MISTAKE)
                    .nextMoves(List.of(Move.builder()
                        .id(id131)
                        .color(Color.BLACK)
                        .build()))
                    .build()))
            .build();

        final List<List<LineMove>> moves = this.bookService
            .buildChapterLines(move, Color.WHITE)
            .toList();

        assertThat(moves).hasSize(3);
        assertThat(moves.getFirst().getFirst().getComment())
            .isEqualTo("comment");
        assertThat(moves.getFirst())
            .extracting(LineMove::getMoveId)
            .containsExactly(id1, id11, id111, id1111);
        assertThat(moves.get(1))
            .extracting(LineMove::getMoveId)
            .containsExactly(id1, id11, id112, id1121);
        assertThat(moves.getLast())
            .extracting(LineMove::getMoveId)
            .containsExactly(id1, id12);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        chapter-1 | chapter-2 | true  | true  | chapter-1 | true  | true  | true
        chapter-1 | chapter-2 | true  | true  | chapter-1 | false | false | true
        chapter-1 | chapter-2 | false | true  | chapter-2 | false | false | false
        chapter-1 | chapter-2 | false | false | chapter-1 | true  | true  | false
        """)
    void toggleChapter(final String chapter1,
                       final String chapter2,
                       final boolean initialStatusChapter1,
                       final boolean initialStatusChapter2,
                       final String toggledChapterId,
                       final boolean askedStatus,
                       final boolean expectedStatusChapter1,
                       final boolean expectedStatusChapter2) {

        final UUID bookId = UUID.randomUUID();
        final Map<String, UUID> chapters = Map.of(
            chapter1, UUID.randomUUID(),
            chapter2, UUID.randomUUID());

        BookBuilder bookBuilder = BookBuilder.builder()
            .id(bookId)
            .withChapter(chapter -> chapter
                .id(chapters.get(chapter1))
                .enabled(initialStatusChapter1))
            .withChapter(chapter -> chapter
                .id(chapters.get(chapter2))
                .enabled(initialStatusChapter2));

        if (initialStatusChapter1)
            bookBuilder = bookBuilder
                .withLine(line -> line
                    .chapterId(chapters.get(chapter1)));

        if (initialStatusChapter2)
            bookBuilder = bookBuilder
                .withLine(line -> line
                    .chapterId(chapters.get(chapter2)));

        Mockito
            .when(this.bookRepositoryMock.findById(bookId))
            .thenReturn(Mono.just(bookBuilder.build()));

        Mockito
            .when(this.bookRepositoryMock.update(Mockito.any()))
            .thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0)));

        this.bookService.toggleChapter(
                bookId,
                chapters.get(toggledChapterId),
                askedStatus)
            .as(StepVerifier::create)
            .consumeNextWith(b -> {
                assertThat(b).isNotNull();
                assertChapter(b, chapters.get(chapter1), expectedStatusChapter1);
                assertChapter(b, chapters.get(chapter2), expectedStatusChapter2);
            })
            .verifyComplete();
    }

    private void assertChapter(final Book book,
                               final UUID chapterId,
                               final boolean expectedStatus) {

        final boolean actualStatus = book.getChapters()
            .stream()
            .filter(c -> c.getId().equals(chapterId))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Chapter not found"))
            .isEnabled();

        assertThat(actualStatus).isEqualTo(expectedStatus);

        if (!expectedStatus) {
            assertThat(book.getLines())
                .extracting(Line::getChapterId)
                .doesNotContain(chapterId);
        }
        // TODO When refresh is implemented, check that lines are refreshed
    }

}
