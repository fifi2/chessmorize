package io.github.fifi2.chessmorize.service.pgn;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * PGN Parser
 * - expected to work on Lichess PGN exports
 * - parse the PGN and feed the package model, starting on PgnGame
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class PgnParser {

    private static final String EVENT_TAG_START = "[Event \"";
    private static final Pattern GAMES_DELIMITER_PATTERN = Pattern.compile(
        "\\" + EVENT_TAG_START);
    private static final Pattern GAME_TAGS_DELIMITER_PATTERN = Pattern.compile(
        "(?:(?<tags>.*?)\\n\\n)?\\s*(?<game>.*)",
        Pattern.DOTALL);
    private static final Pattern TAG_PATTERN = Pattern.compile(
        "\\[(?<tag>\\w+) \"(?<value>.*?)\"]");
    private static final String ANNOTATIONS_REGEX = "\\{\\s\\[%[^}]*]\\s}";
    private static final Pattern GAME_COMMENT_PATTERN = Pattern.compile(
        "^(?:\\{\\s+(?<gameComment>.*?)\\s+})?\\s*"
            + "(?:(?<gameAnnotations>" + ANNOTATIONS_REGEX + ")\\s*)?"
            + "(?<game>.*)\\s*",
        Pattern.DOTALL);
    private static final String PLY_REGEX = "\\d+\\.+";
    private static final String STANDARD_MOVE_REGEX =
        "[RNBQK]?(?:[a-h1-8]|[a-h][1-8])?x?[a-h][1-8](?:=[RNKQ])?";
    private static final String CASTLE_REGEX = "O(?:-O){1,2}";
    private static final String NAG_REGEX = "[?!]{1,2}";
    private static final String COMMENT_REGEX = "\\{\\s+[^(?:\\[%)].*?\\s+}";
    private static final Pattern MOVE_PATTERN = Pattern.compile(
        "\\s*(?:" + PLY_REGEX + "\\s+)?"
            + "(?<san>(?:" + STANDARD_MOVE_REGEX + "|" + CASTLE_REGEX + ")[+#]?)(?<nag>" + NAG_REGEX + ")?\\s*"
            + "(?:(?<comment>" + COMMENT_REGEX + ")\\s*)?"
            + "(?:(?<annotations>" + ANNOTATIONS_REGEX + ")\\s*)?",
        Pattern.DOTALL);
    private static final char VARIATION_START = '(';
    private static final char VARIATION_END = ')';
    private static final char COMMENT_START = '{';
    private static final char COMMENT_END = '}';
    private static final String ANNOTATION_PART_START = "[";
    private static final String ANNOTATION_PART_END = "]";
    private static final String COMMENT_START_END_REGEX = "["
        + COMMENT_START
        + COMMENT_END
        + "]";
    private static final String ANNOTATIONS_SPLIT_REGEX = "]\\[";
    private static final String ESCAPE_SIGN = "%";
    private static final String ANNOTATION_VALUE_SPLIT_REGEX = ",";
    private static final String EMPTY = "";

    record PgnTagsGame(String tags,
                       String game) {
    }

    record PgnVariations(String pgn,
                         List<String> variations) {
    }

    /**
     * Parse the PGN String and create a list of PgnGame
     *
     * @param pgn The PGN coming from Lichess
     * @return A list of PgnGame
     */
    public static List<PgnGame> parse(final String pgn) {

        return splitGames(pgn)
            .parallel()
            .map(PgnParser::splitGameAndTags)
            .map(PgnParser::buildGame)
            .toList();
    }

    /**
     * Split the full PGN in a stream of games. The split is made based on the
     * event tag.
     *
     * @param pgn The full PGN
     * @return A Stream of individual PGN String (having both tags and game).
     */
    static Stream<String> splitGames(final String pgn) {

        return Arrays.stream(GAMES_DELIMITER_PATTERN.split(pgn))
            .skip(1)
            .map(EVENT_TAG_START::concat)
            .map(String::strip);
    }

    /**
     * Split an individual game PGN in two parts : tags and game.
     *
     * @param pgn String the individual PGN game
     * @return A PgnTagsGame record having tags and game set.
     */
    static PgnTagsGame splitGameAndTags(final String pgn) {

        return GAME_TAGS_DELIMITER_PATTERN.matcher(pgn)
            .results()
            .findAny()
            .map(matchResult -> new PgnTagsGame(
                matchResult.group("tags"),
                matchResult.group("game")
            ))
            .orElse(null);
    }

    /**
     * Build a PgnGame from a PgnTagsGame record. Set the root level data of
     * the PgnGame, such as the tags or comment, and launch the recursive
     * parsing of the PGN game.
     *
     * @param pgnTagsGame A PgnTagsGame record
     * @return A fully-qualified PgnGame
     */
    static PgnGame buildGame(final PgnTagsGame pgnTagsGame) {

        return GAME_COMMENT_PATTERN.matcher(pgnTagsGame.game())
            .results()
            .findAny()
            .map(matchResult -> PgnGame.builder()
                .tags(getTags(pgnTagsGame.tags()))
                .comment(Optional.ofNullable(matchResult.group("gameComment"))
                    .map(String::strip)
                    .orElse(null))
                .annotations(buildAnnotations(matchResult.group("gameAnnotations")))
                .nodes(splitGameByRootNodes(matchResult.group("game"))
                    .stream()
                    .map(pgn -> PgnParser.parseGame(pgn, new Board()))
                    .toList())
                .build())
            .orElse(null);
    }

    /**
     * Root variations can't be detected by PgnParser.parseGame() method because
     * our model expects to know the variations when creating the previous move.
     * However, the PGN notation set the variation next to the main one. This
     * method detects the root variations to run multiple parsings based on the
     * return value.
     *
     * @param pgn The PGN to parse.
     * @return A list of PGN split by root variations.
     */
    static List<String> splitGameByRootNodes(final String pgn) {

        return Optional.of(MOVE_PATTERN.matcher(pgn))
            .filter(Matcher::find)
            .stream()
            .flatMap(matcher -> {
                final PgnVariations pgnVariations = splitVariations(matcher.replaceFirst(EMPTY));
                return Stream.concat(
                    Stream.of(matcher.group().stripTrailing() + " " + pgnVariations.pgn().stripTrailing()),
                    pgnVariations.variations().stream());
            })
            .toList();
    }

    /**
     * Return a Map of tags from a String of tags.
     *
     * @param tags The tags of the game
     * @return A Map of tags (key = tag name, value = tag value).
     */
    static Map<String, String> getTags(final String tags) {

        return TAG_PATTERN.matcher(tags)
            .results()
            .collect(Collectors.toMap(
                matchResult -> matchResult.group("tag"),
                matchResult -> matchResult.group("value")
            ));
    }

    /**
     * Parse an individual PGN game as a PgnNode
     *
     * @param pgn   The individual PGN game.
     * @param board The Board which will help following the moves and building
     *              the FEN.
     * @return A PgnNode
     */
    private static PgnNode parseGame(String pgn,
                                     final Board board) {

        final Matcher moveMatcher = MOVE_PATTERN.matcher(pgn);
        if (!moveMatcher.find()) {
            return null;
        }

        pgn = moveMatcher.replaceFirst(EMPTY).stripLeading();
        pgn = splitVariations(pgn).pgn();

        final Matcher nextMoveMatcher = MOVE_PATTERN.matcher(pgn);
        final List<String> variations = new ArrayList<>();

        if (nextMoveMatcher.find()) {
            pgn = nextMoveMatcher.replaceFirst(EMPTY).stripLeading();
            final PgnVariations pgnVariations = splitVariations(pgn);
            pgn = pgnVariations.pgn().strip();
            pgn = pgn.isEmpty()
                ? nextMoveMatcher.group().strip()
                : nextMoveMatcher.group().strip() + " " + pgn;
            variations.add(pgn);
            variations.addAll(pgnVariations.variations());
        }

        final String san = moveMatcher.group("san");
        board.move(san);
        final String fen = board.getFen();

        return PgnNode.builder()
            .san(san)
            .nag(PgnNag.fromGlyph(moveMatcher.group("nag"))
                .orElse(null))
            .comment(Optional.ofNullable(moveMatcher.group("comment"))
                .map(com -> com.replaceAll(COMMENT_START_END_REGEX, EMPTY))
                .map(String::strip)
                .orElse(null))
            .annotations(buildAnnotations(moveMatcher.group("annotations")))
            .variations(variations.stream()
                .map(pgnVariation -> PgnParser.parseGame(pgnVariation, new Board(fen)))
                .filter(Objects::nonNull)
                .toList())
            .fen(fen)
            .uci(board.getUci())
            .build();
    }

    /**
     * Build annotation object from a comment markup annotations extract.
     * See <a href="https://old.chesstempo.com/user-guide/en/pgnViewerCommentAnnotations.html">specifications</a>.
     *
     * @param input The raw comment.
     *              E.g. "{ [%csl Rd4][%cal Gd4c5,Rd4d5,Bd4e5,Yd7d5] }"
     * @return A map of coded annotations by type (e.g. csl or cal). The values
     * are the lists of coded annotations (e.g. Ra3 for a csl or Ge2e4 for an
     * arrow).
     */
    static Map<String, List<String>> buildAnnotations(final String input) {

        return Optional.ofNullable(input)
            .map(i -> i.replaceAll(COMMENT_START_END_REGEX, EMPTY))
            .map(String::strip)
            .filter(i -> i.startsWith(ANNOTATION_PART_START))
            .filter(i -> i.endsWith(ANNOTATION_PART_END))
            .map(i -> i.substring(1, i.length() - 1))
            .map(i -> Arrays.asList(i.split(ANNOTATIONS_SPLIT_REGEX)))
            .stream()
            .flatMap(List::stream)
            .filter(i -> i.startsWith(ESCAPE_SIGN))
            .map(i -> i.substring(1))
            .map(i -> Arrays.asList(i.split(" ")))
            .collect(Collectors.toMap(
                List::getFirst,
                i -> Arrays.asList(
                    i.getLast().split(ANNOTATION_VALUE_SPLIT_REGEX))
            ));
    }

    /**
     * Put aside next coming variations in a PGN String.
     *
     * @param pgn The partial PGN which needs to be rid of its coming
     *            variations.
     * @return The PGN without the next coming variations and the list of
     * these variations (encapsulated in a PgnVariations record).
     */
    static PgnVariations splitVariations(final String pgn) {

        String remainingPgn = Optional.ofNullable(pgn).orElse(EMPTY);
        final List<String> variations = new ArrayList<>();

        while (remainingPgn.startsWith(String.valueOf(VARIATION_START))) {
            final String variation = getNextVariation(remainingPgn);
            variations.add(variation);
            remainingPgn = remainingPgn
                .substring(variation.length() + 2)
                .stripLeading();
        }

        return new PgnVariations(remainingPgn, variations);
    }

    /**
     * Extract the next full variation (including sub-variations).
     *
     * @param input The partial PGN.
     * @return The next full variation.
     */
    static String getNextVariation(final String input) {

        int parentheses = 0;
        int index = 0;
        boolean isNotInAComment = true;

        for (char c : input.toCharArray()) {
            if (isNotInAComment) {
                if (c == VARIATION_START) {
                    parentheses++;
                } else if (c == VARIATION_END) {
                    parentheses--;
                }
            }
            if (c == COMMENT_START) {
                isNotInAComment = false;
            } else if (c == COMMENT_END) {
                isNotInAComment = true;
            }

            if (parentheses == 0) {
                break;
            }

            index++;
        }

        return input.substring(1, index);
    }

}
