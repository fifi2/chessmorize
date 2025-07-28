package io.github.fifi2.chessmorize.service.pgn;

import io.github.fifi2.chessmorize.helper.converter.StringToList;
import io.github.fifi2.chessmorize.service.pgn.PgnParser.PgnTagsGame;
import io.github.fifi2.chessmorize.service.pgn.PgnParser.PgnVariations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PgnParserTest {

    @Test
    void parse() {

        final String pgn = """
            [Event "firstGame"]
            [Result "*"]

            1. e4 c6 2. d4 d5 3. e5 Bf5 *


            [Event "secondGame"]

            1. e4 c6 2. d4 d5 3. exd5 cxd5 *

            """;

        List<PgnGame> pgnGames = PgnParser.parse(pgn);
        assertThat(pgnGames).hasSize(2);

        // firstGame
        assertThat(pgnGames.getFirst().getTags()).containsExactlyInAnyOrderEntriesOf(Map.of(
            "Event", "firstGame",
            "Result", "*"
        ));
        assertThat(pgnGames.getFirst().getComment()).isNull();

        // secondGame
        assertThat(pgnGames.get(1).getTags()).containsExactlyInAnyOrderEntriesOf(Map.of(
            "Event", "secondGame"
        ));
        assertThat(pgnGames.get(1).getComment()).isNull();
    }

    @Test
    void splitGames_withOneGame() {

        final String pgn = """
            [Event "firstGame"]

            1. e4 c6 2. d4 d5 3. e5 Bf5 *

            """;

        assertThat(PgnParser.splitGames(pgn))
            .hasSize(1)
            .allMatch(game -> game.startsWith("[Event"))
            .allMatch(game -> game.contains("1. e4"));
    }

    @Test
    void splitGames_withSeveralGames() {

        final String pgn = """
            [Event "firstGame"]

            1. e4 c6 2. d4 d5 3. e5 Bf5 *


            [Event "secondGame"]

            1. e4 c6 2. d4 d5 3. exd4 cxd4 *

            """;

        assertThat(PgnParser.splitGames(pgn))
            .hasSize(2)
            .allMatch(game -> game.startsWith("[Event"))
            .allMatch(game -> game.contains("1. e4"));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        1,n,1           | 1 | 1 | true
        1,1             | 0 | 2 | true
        2,n,n,1         | 2 | 1 | true
        2,n,1           | 2 | 1 | true
        2,n,1,n,1,n,n,1 | 2 | 6
        """)
    void splitGameAndTags(@ConvertWith(StringToList.class) List<String> specs,
                          Integer expectedLinesTags,
                          Integer expectedLinesPgn) {

        final String lineBreak = "\n";

        // build a fake pgn based on specs (n is a blank line, any integer is a block of x lines)
        final String fakePgn = specs.stream()
            .map(s -> {
                if ("n".equals(s))
                    return "";
                return IntStream.range(0, Integer.parseInt(s))
                    .mapToObj(i -> UUID.randomUUID().toString())
                    .collect(Collectors.joining(lineBreak));
            })
            .collect(Collectors.joining(lineBreak));

        final PgnTagsGame pgnTagsGame = PgnParser.splitGameAndTags(fakePgn);
        final String tags = pgnTagsGame.tags();

        if (expectedLinesTags.compareTo(0) == 0) {
            assertThat(tags).isNull();
        } else {
            assertThat(tags.split(lineBreak)).hasSize(expectedLinesTags);
        }

        assertThat(pgnTagsGame.game().split(lineBreak)).hasSize(expectedLinesPgn);
    }

    @Test
    void splitGameByRootNodes() {

        final String pgn = """
            1. e4 (1. d4 d5) c6 *
            """;

        final List<String> games = PgnParser.splitGameByRootNodes(pgn);

        assertThat(games).hasSize(2);
        assertThat(games.getFirst()).isEqualTo("1. e4 c6 *");
        assertThat(games.get(1)).isEqualTo("1. d4 d5");
    }

    @Test
    void getTags() {

        final String pgn = """
            [Event "eventName"]
            [Whatever "value"]
            """;

        assertThat(PgnParser.getTags(pgn))
            .containsExactlyInAnyOrderEntriesOf(Map.of(
                "Event", "eventName",
                "Whatever", "value"
            ));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
                                                         |         |         |
        { [%csl Rd4] }                                   | csl     | Rd4     |
        { [%csl Rd4,Ge3] }                               | csl     | Rd4,Ge3 |
        { [%cal Gd4c5] }                                 | cal     |         | Gd4c5
        { [%cal Gd4c5,Rd4d5,Bd4e5,Yd7d5] }               | cal     |         | Gd4c5,Rd4d5,Bd4e5,Yd7d5
        { [%csl Rd4,Ge3][%cal Gd4c5,Rd4d5,Bd4e5,Yd7d5] } | csl,cal | Rd4,Ge3 | Gd4c5,Rd4d5,Bd4e5,Yd7d5
        """)
    void buildAnnotations(
        String input,
        @ConvertWith(StringToList.class) List<String> expectedKeys,
        @ConvertWith(StringToList.class) List<String> expectedCsl,
        @ConvertWith(StringToList.class) List<String> expectedCal) {

        final Map<String, List<String>> annotations = PgnParser.buildAnnotations(input);

        assertThat(annotations)
            .isNotNull()
            .containsOnlyKeys(expectedKeys);

        Map.of("csl", expectedCsl, "cal", expectedCal)
            .forEach((key, value) -> {
                if (value.isEmpty()) {
                    assertThat(annotations.get(key)).isNull();
                } else {
                    assertThat(annotations.get(key))
                        .containsExactlyInAnyOrderElementsOf(value);
                }
            });
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
                                |                         |
        something (var1) (var2) | something (var1) (var2) |
        (var1) something        | something               | var1
        (var1) (var2) something | something               | var1,var2
        """)
    void splitVariations(
        String pgn,
        String expectedPgn,
        @ConvertWith(StringToList.class) List<String> expectedVariations) {

        final PgnVariations pgnVariations = PgnParser.splitVariations(pgn);

        assertThat(pgnVariations.pgn())
            .isEqualTo(Optional.ofNullable(expectedPgn).orElse(""));
        assertThat(pgnVariations.variations())
            .containsExactlyElementsOf(expectedVariations);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        (123) | 123
        (123 (45)) | 123 (45)
        (123 { 45 (67) } 8. 9) 10 | 123 { 45 (67) } 8. 9
        """)
    void getNextVariation(String pgn, String nextVariation) {

        assertThat(PgnParser.getNextVariation(pgn))
            .isEqualTo(nextVariation);
    }

    @Test
    void buildGame_basicPgn() {

        final String pgn = "1. d4 d5 *";
        final PgnGame game = PgnParser.buildGame(new PgnTagsGame("", pgn));

        assertThat(game.getComment()).isNull();
        assertThat(game.getNodes()).hasSize(1);

        // @formatter:off
        assertNode(game.getNodes().getFirst(), "d4", null, null, null,
            d5 -> assertNode(d5, "d5", null, null, null));
        // @formatter:on
    }

    @Test
    void buildGame_withMainComment() {

        final String pgn = """
            { This is a comment including (parentheses). }
            1. d4 *
            """;

        final PgnGame game = PgnParser.buildGame(new PgnTagsGame("", pgn));

        assertThat(game.getComment())
            .isEqualTo("This is a comment including (parentheses).");
        assertThat(game.getNodes()).hasSize(1);

        assertNode(game.getNodes().getFirst(), "d4", null, null, null);
    }

    @Test
    void buildGame_withComments() {

        final String pgn = """
            1. d4 { I like playing d4 } 1... d5 { Most common response } 2. c4 { Queen's gambit } 2... dxc4 { Black takes! } *
            """;

        final PgnGame game = PgnParser.buildGame(new PgnTagsGame("", pgn));

        assertThat(game.getComment()).isNull();
        assertThat(game.getNodes()).hasSize(1);

        // @formatter:off
        assertNode(game.getNodes().getFirst(), "d4", null, "I like playing d4", null,
            d5 -> assertNode(d5, "d5", null, "Most common response", null,
            c4 -> assertNode(c4, "c4", null, "Queen's gambit", null,
            dxc4 -> assertNode(dxc4, "dxc4", null, "Black takes!", null))));
        // @formatter:on
    }

    @Test
    void buildGame_withBasicVariations() {

        final String pgn = """
            1. d4 d5 { A comment here } (1... e5 2. dxe5) 2. c4 (2. Nf3 { A comment }) 2... dxc4 (2... e6 3. Nc3 dxc4 (3... Nf6 4. Bg5) 4. e4 Nf6 { Again, a comment }) 3. Nf3 *
            """;

        final PgnGame game = PgnParser.buildGame(new PgnTagsGame("", pgn));

        assertThat(game.getComment()).isNull();
        assertThat(game.getNodes()).hasSize(1);

        // @formatter:off
        assertNode(game.getNodes().getFirst(), "d4", null, null, null,
            d5 -> assertNode(d5, "d5", null, "A comment here", null,
                c4 -> assertNode(c4, "c4", null, null, null,
                    dxc4 -> assertNode(dxc4, "dxc4", null, null, null,
                        nf3 -> assertNode(nf3, "Nf3", null, null, null)),
                    e6 -> assertNode(e6, "e6", null, null, null,
                        nc3 -> assertNode(nc3, "Nc3", null, null, null,
                            dxc4 -> assertNode(dxc4, "dxc4", null, null, null,
                                e4 -> assertNode(e4, "e4", null, null, null,
                                nf6 -> assertNode(nf6, "Nf6", null, "Again, a comment", null))),
                            nf6 -> assertNode(nf6, "Nf6", null, null, null,
                                bg5 -> assertNode(bg5, "Bg5", null, null, null))))),
                nf3 -> assertNode(nf3, "Nf3", null, "A comment", null)),
            e5 -> assertNode(e5, "e5", null, null, null,
                dxe5 -> assertNode(dxe5, "dxe5", null, null, null)));
        // @formatter:on
    }

    @Test
    void buildGame_withNags() {

        final String pgn = """
            1. d4! d5 2. c4!! dxc4!? 3. Nc3?! Qd5?? (3... Nf6 4. e3 { A comment in a variation }) 4. Nxd5 Kd7? { A comment } *
            """;

        final PgnGame game = PgnParser.buildGame(new PgnTagsGame("", pgn));

        assertThat(game.getComment()).isNull();
        assertThat(game.getNodes()).hasSize(1);

        // @formatter:off
        assertNode(game.getNodes().getFirst(), "d4", PgnNag.GOOD_MOVE, null, null,
            d5 -> assertNode(d5, "d5", null, null, null,
            c4 -> assertNode(c4, "c4", PgnNag.BRILLIANT_MOVE, null, null,
            dxc4 -> assertNode(dxc4, "dxc4", PgnNag.SPECULATIVE_MOVE, null, null,
            nc3 -> assertNode(nc3, "Nc3", PgnNag.DUBIOUS_MOVE, null, null,
                qd5 -> assertNode(qd5, "Qd5", PgnNag.BLUNDER, null, null,
                    nxd5 -> assertNode(nxd5, "Nxd5", null, null, null,
                    kd7 -> assertNode(kd7, "Kd7", PgnNag.MISTAKE, "A comment", null))),
                nf6 -> assertNode(nf6, "Nf6", null, null, null,
                    e3 -> assertNode(e3, "e3", null, "A comment in a variation", null)))))));
        // @formatter:on
    }

    @Test
    void buildGame_withCastling() {

        final String pgn = """
            1. d4 d5 2. c4 dxc4 3. Nf3 Nf6 4. e3 b5 5. a4 b4 6. Bxc4 Nc6 7. O-O Bg4 8. Qb3 Qd7? 9. Nbd2? O-O-O { Castle } *
            """;

        final PgnGame game = PgnParser.buildGame(new PgnTagsGame("", pgn));

        assertThat(game.getComment()).isNull();
        assertThat(game.getNodes()).hasSize(1);

        // @formatter:off
        assertNode(game.getNodes().getFirst(), "d4", null, null, null,
            d5 -> assertNode(d5, "d5", null, null, null,
            c4 -> assertNode(c4, "c4", null, null, null,
            dxc4 -> assertNode(dxc4, "dxc4", null, null, null,
            nf3 -> assertNode(nf3, "Nf3", null, null, null,
            nf6 -> assertNode(nf6, "Nf6", null, null, null,
            e3 -> assertNode(e3, "e3", null, null, null,
            b5 -> assertNode(b5, "b5", null, null, null,
            a4 -> assertNode(a4, "a4", null, null, null,
            b4 -> assertNode(b4, "b4", null, null, null,
            bxc4 -> assertNode(bxc4, "Bxc4", null, null, null,
            nc6 -> assertNode(nc6, "Nc6", null, null, null,
            oo -> assertNode(oo, "O-O", null, null, null,
            bg4 -> assertNode(bg4, "Bg4", null, null, null,
            qb3 -> assertNode(qb3, "Qb3", null, null, null,
            qd7 -> assertNode(qd7, "Qd7", PgnNag.MISTAKE, null, null,
            nbd2 -> assertNode(nbd2, "Nbd2", PgnNag.MISTAKE, null, null,
            ooo -> assertNode(ooo, "O-O-O", null, "Castle", null))))))))))))))))));
        // @formatter:on
    }

    @Test
    void buildGame_withArrowsAndCircles() {

        final String pgn = """
            1. e4 c6 { [%cal Gc6d5,Bd7d5] } 2. d4 { [%csl Rd4][%cal Gd4c5,Rd4d5,Bd4e5,Yd7d5] } 2... d5 { A comment and some arrows and circles } { [%csl Gd5,Ye4][%cal Be4d5,Ge4e5,Rb1c3,Yf2f3] } *
            """;

        final PgnGame game = PgnParser.buildGame(new PgnTagsGame("", pgn));

        assertThat(game.getComment()).isNull();
        assertThat(game.getNodes()).hasSize(1);

        // @formatter:off
        assertNode(game.getNodes().getFirst(), "e4", null, null, null,
            c6 -> assertNode(c6, "c6", null, null, "cal:Gc6d5,Bd7d5",
            d4 -> assertNode(d4, "d4", null, null, "csl:Rd4;cal:Gd4c5,Rd4d5,Bd4e5,Yd7d5",
            d5 -> assertNode(d5, "d5", null, "A comment and some arrows and circles", "csl:Gd5,Ye4;cal:Be4d5,Ge4e5,Rb1c3,Yf2f3"))));
        // @formatter:on
    }

    @Test
    void buildGame_multipleRootVariations() {

        final String pgn = """
            1. e4 (1. d4 d5) c6 *
            """;

        final PgnGame game = PgnParser.buildGame(new PgnTagsGame("", pgn));

        assertThat(game.getComment()).isNull();
        assertThat(game.getNodes()).hasSize(2);

        // @formatter:off
        assertNode(game.getNodes().getFirst(), "e4", null, null, null,
            c6 -> assertNode(c6, "c6", null, null, null));
        assertNode(game.getNodes().get(1), "d4", null, null, null,
            d5 -> assertNode(d5, "d5", null, null, null));
        // @formatter:on
    }

    @Test
    void buildGame_enPassant() {

        final String pgn = """
            1. d4 a6 2. d5 e5 3. dxe6 *
            """;

        final PgnGame game = PgnParser.buildGame(new PgnTagsGame("", pgn));

        assertThat(game.getComment()).isNull();
        assertThat(game.getNodes()).hasSize(1);

        // @formatter:off
        assertNode(game.getNodes().getFirst(), "d4", null, null, null,
            a6 -> assertNode(a6, "a6", null, null, null,
            d5 -> assertNode(d5, "d5", null, null, null,
            e5 -> assertNode(e5, "e5", null, null, null,
            dxe6 -> assertNode(dxe6, "dxe6", null, null, null)))));
        // @formatter:on
    }

    @Test
    void buildGame_commentsOnMultipleLines() {

        final String pgn = """
            { A comment
            that takes
            three lines }
            1. e4 c6 { Another one
            on several
            lines too } *
            """;

        final PgnGame game = PgnParser.buildGame(new PgnTagsGame("", pgn));

        assertThat(game.getComment())
            .isEqualTo("A comment\nthat takes\nthree lines");

        // @formatter:off
        assertNode(game.getNodes().getFirst(), "e4", null, null, null,
            c6 -> assertNode(c6, "c6", null, "Another one\non several\nlines too", null));
        // @formatter:on
    }

    @Test
    void buildGame_promotion() {

        final String pgn = """
            1. d4 d5 2. c4 e5 3. dxe5 d4 4. e3 Bb4+ 5. Bd2 dxe3 6. Bxb4?? exf2+ 7. Ke2 fxg1=N+ 8. Ke1 *
            """;

        final PgnGame game = PgnParser.buildGame(new PgnTagsGame("", pgn));

        // @formatter:off
        assertNode(game.getNodes().getFirst(), "d4", null, null, null,
            d5 -> assertNode(d5, "d5", null, null, null,
            c4 -> assertNode(c4, "c4", null, null, null,
            e5 -> assertNode(e5, "e5", null, null, null,
            dxe5 -> assertNode(dxe5, "dxe5", null, null, null,
            d4 -> assertNode(d4, "d4", null, null, null,
            e3 -> assertNode(e3, "e3", null, null, null,
            bb4 -> assertNode(bb4, "Bb4+", null, null, null,
            bd2 -> assertNode(bd2, "Bd2", null, null, null,
            dxe3 -> assertNode(dxe3, "dxe3", null, null, null,
            bxb4 -> assertNode(bxb4, "Bxb4", PgnNag.BLUNDER, null, null,
            exf2 -> assertNode(exf2, "exf2+", null, null, null,
            ke2 -> assertNode(ke2, "Ke2", null, null, null,
            fxg1 -> assertNode(fxg1, "fxg1=N+", null, null, null,
            ke1 -> assertNode(ke1, "Ke1", null, null, null)))))))))))))));
        // @formatter:on
    }

    @Test
    void buildGame_annotationsBeforeFirstMove() {

        final String pgn = """
            { A global comment.
            And some annotations before the first move. } { [%csl Yd5][%cal Ge2e4,Rc7c6] }
            1. e4 *
            """;

        final PgnGame game = PgnParser.buildGame(new PgnTagsGame("", pgn));

        assertThat(game.getComment())
            .isEqualTo("A global comment.\n"
                + "And some annotations before the first move.");

        assertAnnotations(
            game.getAnnotations(),
            "csl:Yd5;cal:Ge2e4,Rc7c6");

        assertNode(
            game.getNodes().getFirst(),
            "e4",
            null,
            null,
            null);
    }

    private static void assertNode(PgnNode node,
                                   String expectedSan,
                                   PgnNag expectedNag,
                                   String expectedComment,
                                   String expectedAnnotations,
                                   NodeAssertion... assertions) {

        // san
        assertThat(node.getSan()).isEqualTo(expectedSan);

        // nag
        assertThat(node.getNag()).isEqualTo(expectedNag);

        // comment
        Optional.ofNullable(expectedComment)
            .ifPresentOrElse(
                c -> assertThat(node.getComment()).isEqualTo(c),
                () -> assertThat(node.getComment()).isNull()
            );

        // annotations
        assertAnnotations(node.getAnnotations(), expectedAnnotations);

        // board data
        assertThat(node.getFen()).isNotNull();
        assertThat(node.getUci()).isNotNull();

        // variations
        int index = 0;
        assertThat(node.getVariations()).hasSize(assertions.length);
        for (NodeAssertion assertion : assertions) {
            assertion.runAssert(node.getVariations().get(index));
            index++;
        }
    }

    private static void assertAnnotations(
        final Map<String, List<String>> annotations,
        final String expectedAnnotations) {

        final Map<String, List<String>> expectedAnnotationsMap = Optional
            .ofNullable(expectedAnnotations)
            .stream()
            .flatMap(i -> Arrays.stream(i.split(";")))
            .map(i -> Arrays.asList(i.split(":")))
            .collect(Collectors.toMap(
                List::getFirst,
                i -> Arrays.asList(i.getLast().split(","))
            ));

        assertThat(annotations)
            .containsOnlyKeys(expectedAnnotationsMap.keySet());
        annotations.forEach((key, annotation) ->
            assertThat(annotation)
                .containsExactlyElementsOf(expectedAnnotationsMap.get(key)));
    }

}
