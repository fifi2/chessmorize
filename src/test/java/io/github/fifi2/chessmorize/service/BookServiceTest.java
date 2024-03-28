package io.github.fifi2.chessmorize.service;

import io.github.fifi2.chessmorize.api.LichessApiClient;
import io.github.fifi2.chessmorize.converter.PgnGamesToBookConverter;
import io.github.fifi2.chessmorize.error.exception.pgn.PgnException;
import io.github.fifi2.chessmorize.model.*;
import io.github.fifi2.chessmorize.repository.BookRepository;
import io.github.fifi2.chessmorize.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

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

    @InjectMocks
    private BookService bookService;

    @Test
    void createBook() {

        Mockito
            .when(this.lichessApiClientMock.getStudyPGN(STUDY_ID))
            .thenReturn(Mono.just("1. d4 d5 *"));

        Mockito
            .when(this.pgnGamesToBookConverterMock.convert(
                Mockito.anyList(),
                Mockito.eq(STUDY_ID)))
            .thenReturn(Book.builder()
                .studyId(STUDY_ID)
                .chapters(List.of())
                .build());

        Mockito
            .when(this.bookRepositoryMock.save(Mockito.any()))
            .thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0)));

        StepVerifier
            .create(this.bookService.createBook(STUDY_ID))
            .expectNextMatches(book -> STUDY_ID.equals(book.getStudyId()))
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
            .create(this.bookService.createBook(STUDY_ID))
            .expectError(PgnException.class)
            .verify();

        Mockito
            .verify(this.pgnGamesToBookConverterMock, Mockito.never())
            .convert(Mockito.anyList(), Mockito.anyString());

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

    @Test
    void createLines() {

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

        final List<Line> lines = this.bookService.createLines(book);

        assertThat(lines).hasSize(4);

        assertThat(lines.getFirst().getChapterId()).isEqualTo(chapterId1);
        assertThat(lines.getFirst().getBoxId()).isZero();
        assertThat(lines.getFirst().getMoves())
            .extracting(LineMove::getMoveId)
            .containsExactly(id1, id11);

        assertThat(lines.get(1).getChapterId()).isEqualTo(chapterId1);
        assertThat(lines.get(1).getBoxId()).isZero();
        assertThat(lines.get(1).getMoves())
            .extracting(LineMove::getMoveId)
            .containsExactly(id1, id12);

        assertThat(lines.get(2).getChapterId()).isEqualTo(chapterId1);
        assertThat(lines.get(2).getBoxId()).isZero();
        assertThat(lines.get(2).getMoves())
            .extracting(LineMove::getMoveId)
            .containsExactly(id2);

        assertThat(lines.getLast().getChapterId()).isEqualTo(chapterId2);
        assertThat(lines.getLast().getBoxId()).isZero();
        assertThat(lines.getLast().getMoves())
            .extracting(LineMove::getMoveId)
            .containsExactly(id3, id31);
    }

    @Test
    void buildChapterLines() {

        final UUID id1 = UUID.randomUUID();
        final UUID id11 = UUID.randomUUID();
        final UUID id12 = UUID.randomUUID();
        final UUID id111 = UUID.randomUUID();
        final UUID id112 = UUID.randomUUID();
        final UUID id113 = UUID.randomUUID();
        final UUID id121 = UUID.randomUUID();
        final UUID id1111 = UUID.randomUUID();

        final Move move = Move.builder()
            .id(id1)
            .nextMoves(List.of(
                Move.builder()
                    .id(id11)
                    .nextMoves(List.of(
                        Move.builder()
                            .id(id111)
                            .nextMoves(List.of(
                                Move.builder()
                                    .id(id1111)
                                    .build()))
                            .build(),
                        Move.builder()
                            .id(id112)
                            .build(),
                        Move.builder()
                            .id(id113)
                            .build()))
                    .build(),
                Move.builder()
                    .id(id12)
                    .nextMoves(List.of(
                        Move.builder()
                            .id(id121)
                            .build()))
                    .build()))
            .build();

        final List<List<LineMove>> moves = this.bookService
            .buildChapterLines(move)
            .toList();

        assertThat(moves).hasSize(4);
        assertThat(moves.getFirst())
            .extracting(LineMove::getMoveId)
            .containsExactly(id1, id11, id111, id1111);
        assertThat(moves.get(1))
            .extracting(LineMove::getMoveId)
            .containsExactly(id1, id11, id112);
        assertThat(moves.get(2))
            .extracting(LineMove::getMoveId)
            .containsExactly(id1, id11, id113);
        assertThat(moves.getLast())
            .extracting(LineMove::getMoveId)
            .containsExactly(id1, id12, id121);
    }

}
