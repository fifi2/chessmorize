package io.github.fifi2.chessmorize.converter;

import io.github.fifi2.chessmorize.model.Book;
import io.github.fifi2.chessmorize.model.Chapter;
import io.github.fifi2.chessmorize.model.Move;
import io.github.fifi2.chessmorize.model.Nag;
import io.github.fifi2.chessmorize.service.pgn.PgnGame;
import io.github.fifi2.chessmorize.service.pgn.PgnNag;
import io.github.fifi2.chessmorize.service.pgn.PgnNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PgnGamesToBookConverterTest {

    private final PgnGamesToBookConverter converter =
        new PgnGamesToBookConverter();

    @Test
    void convert() {

        final String studyId = "studyId";
        final Book book = this.converter.convert(
            List.of(
                PgnGame.builder().build(),
                PgnGame.builder().build()),
            studyId);

        assertThat(book.getId()).isInstanceOf(UUID.class);
        assertThat(book.getStudyId()).isEqualTo(studyId);
        assertThat(book.getName()).isNull();
        assertThat(book.getChapters()).hasSize(2);
    }

    @Test
    void getStudyName() {

        final List<PgnGame> pgnGames = Stream.of(
                "Study: Black: Part 1: chapter 1",
                "Study: Black: Part 2: chapter 1",
                "Study: Black: Part 2: chapter 2")
            .map(chapterName -> PgnGame.builder()
                .tags(Map.of("Event", chapterName))
                .build())
            .toList();

        assertThat(this.converter.getStudyName(pgnGames))
            .isEqualTo("Study: Black");
    }

    @Test
    void buildChapters() {

        final List<PgnGame> pgnGames = List.of(
            PgnGame.builder()
                .tags(Map.of("Event", "Study: chapter 1"))
                .nodes(List.of(
                    PgnNode.builder().build(),
                    PgnNode.builder().build()))
                .build(),
            PgnGame.builder()
                .tags(Map.of("Event", "Study: chapter 2"))
                .nodes(List.of(PgnNode.builder().build()))
                .build());

        final List<Chapter> chapters = this.converter.buildChapters(
            pgnGames,
            "Study");

        assertThat(chapters).hasSize(2);
        final Chapter firstChapter = chapters.getFirst();
        assertThat(firstChapter.getTitle()).isEqualTo("chapter 1");
        assertThat(firstChapter.getNextMoves()).hasSize(2);
        final Chapter secondChapter = chapters.getLast();
        assertThat(secondChapter.getTitle()).isEqualTo("chapter 2");
        assertThat(secondChapter.getNextMoves()).hasSize(1);
    }

    @DisplayName("Remove study name from chapter name:")
    @ParameterizedTest(
        name = "{index}: with study name <{0}> and chapter name <{1}>")
    @CsvSource(delimiter = '|', textBlock = """
        My study: chapter 1         | My study         | chapter 1
        My study : chapter 1        | My study         | chapter 1
        My study (White): chapter 1 | My study (White) | chapter 1
        """)
    void cleanChapterName(final String chapterName,
                          final String studyName,
                          final String expectedChapterName) {

        final String cleanedChapterName = this.converter.cleanChapterName(
            chapterName,
            studyName);
        assertThat(cleanedChapterName).isEqualTo(expectedChapterName);
    }

    @Test
    void buildMove() {

        final PgnNode pgnNode = PgnNode.builder()
            .san("Nf3")
            .nag(PgnNag.GOOD_MOVE)
            .comment("Knight move")
            .variations(List.of(
                PgnNode.builder().san("Nc6").build(),
                PgnNode.builder().san("c5").build()))
            .fen("rnbqkbnr/ppp2ppp/8/4P3/2Pp4/5N2/PP2PPPP/RNBQKB1R b KQkq - 1 4")
            .uci("g1f3")
            .build();

        final Move move = this.converter.buildMove(pgnNode);

        assertThat(move.getSan()).isEqualTo("Nf3");
        assertThat(move.getUci()).isEqualTo("g1f3");
        assertThat(move.getNag()).isEqualTo(Nag.GOOD_MOVE);
        assertThat(move.getComment()).isEqualTo("Knight move");
        final List<Move> nextMoves = move.getNextMoves();
        assertThat(nextMoves).hasSize(2);
        assertThat(nextMoves.getFirst().getSan()).isEqualTo("Nc6");
        assertThat(nextMoves.getLast().getSan()).isEqualTo("c5");
    }

    @DisplayName("Convert PgnNag to Nag:")
    @ParameterizedTest(name = "{index}: when PgnNag is {0}")
    @CsvSource(delimiter = '|', textBlock = """
                         |
        GOOD_MOVE        | GOOD_MOVE
        MISTAKE          | MISTAKE
        BRILLANT_MOVE    | BRILLANT_MOVE
        BLUNDER          | BLUNDER
        SPECULATIVE_MOVE | SPECULATIVE_MOVE
        DUBIOUS_MOVE     | DUBIOUS_MOVE
        """)
    void buildNag(final PgnNag pgnNag, final Nag nag) {

        assertThat(this.converter.buildNag(pgnNag)).isEqualTo(nag);
    }

}
