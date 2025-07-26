package io.github.fifi2.chessmorize.converter;

import io.github.fifi2.chessmorize.model.*;
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

import static org.assertj.core.api.Assertions.assertThat;

class PgnGamesToBookConverterTest {

    private final PgnGamesToBookConverter converter =
        new PgnGamesToBookConverter();

    @Test
    void convert() {

        final String studyId = "studyId";
        final Book book = this.converter.convert(
            getSamplePngGames(),
            studyId,
            Color.WHITE);

        assertThat(book.getId()).isInstanceOf(UUID.class);
        assertThat(book.getStudyId()).isEqualTo(studyId);
        assertThat(book.getName()).isEqualTo("Study");
        assertThat(book.getColor()).isEqualTo(Color.WHITE);
        assertThat(book.getChapters()).hasSize(2);
    }

    @Test
    void buildChapters() {

        final List<Chapter> chapters = this.converter.buildChapters(
            getSamplePngGames());

        assertThat(chapters).hasSize(2);
        final Chapter firstChapter = chapters.getFirst();
        assertThat(firstChapter.getTitle()).isEqualTo("chapter 1");
        assertThat(firstChapter.getNextMoves()).hasSize(2);
        final Chapter secondChapter = chapters.getLast();
        assertThat(secondChapter.getTitle()).isEqualTo("chapter 2");
        assertThat(secondChapter.getNextMoves()).hasSize(1);
    }

    private static List<PgnGame> getSamplePngGames() {

        return List.of(
            PgnGame.builder()
                .tags(Map.of(
                    "Event", "Study: chapter 1",
                    "StudyName", "Study",
                    "ChapterName", "chapter 1"))
                .nodes(List.of(
                    PgnNode.builder().build(),
                    PgnNode.builder().build()))
                .build(),
            PgnGame.builder()
                .tags(Map.of(
                    "Event", "Study: chapter 2",
                    "StudyName", "Study",
                    "ChapterName", "chapter 2"))
                .nodes(List.of(PgnNode.builder().build()))
                .build());
    }

    @Test
    void buildMove() {

        final Move move = this.converter.buildMove(PgnNode.builder()
            .san("Nf3")
            .nag(PgnNag.GOOD_MOVE)
            .comment("Knight move")
            .variations(List.of(
                PgnNode.builder().san("Nc6").build(),
                PgnNode.builder().san("c5").build()))
            .fen("rnbqkbnr/ppp2ppp/8/4P3/2Pp4/5N2/PP2PPPP/RNBQKB1R b KQkq - 1 4")
            .uci("g1f3")
            .build());

        assertThat(move.getSan()).isEqualTo("Nf3");
        assertThat(move.getUci()).isEqualTo("g1f3");
        assertThat(move.getNag()).isEqualTo(Nag.GOOD_MOVE);
        assertThat(move.getComment()).isEqualTo("Knight move");
        assertThat(move.getColor()).isEqualTo(Color.WHITE);
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

    @DisplayName("Build Color from FEN:")
    @ParameterizedTest(name = "{index}: when FEN is {0} then Color is {1}")
    @CsvSource(delimiter = '|', textBlock = """
        rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1   | WHITE
        rnbqkbnr/pp1ppppp/2p5/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2 | BLACK
        """)
    void buildColorFromFen(final String fen,
                           final Color expectedColor) {

        assertThat(this.converter.buildColorFromFen(fen))
            .isEqualTo(expectedColor);
    }

}
