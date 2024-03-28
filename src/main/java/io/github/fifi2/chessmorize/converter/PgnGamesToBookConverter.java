package io.github.fifi2.chessmorize.converter;

import io.github.fifi2.chessmorize.model.Book;
import io.github.fifi2.chessmorize.model.Chapter;
import io.github.fifi2.chessmorize.model.Move;
import io.github.fifi2.chessmorize.model.Nag;
import io.github.fifi2.chessmorize.service.pgn.PgnGame;
import io.github.fifi2.chessmorize.service.pgn.PgnNag;
import io.github.fifi2.chessmorize.service.pgn.PgnNode;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PgnGamesToBookConverter {

    /**
     * Convert some PgnGames, from Lichess PGN parsing, into a Book.
     *
     * @param pgnGames The list of PgnGames to convert.
     * @return A Book.
     */
    public Book convert(final List<PgnGame> pgnGames,
                        final String studyId) {

        final String studyName = this.getStudyName(pgnGames);
        return Book.builder()
            .id(UUID.randomUUID())
            .studyId(studyId)
            .name(studyName)
            .chapters(this.buildChapters(pgnGames, studyName))
            .build();
    }

    /**
     * Guess the study name from the chapter names. Lichess prefix all chapter
     * names with the study name, so it is possible to guess the study name by
     * looking for the longest common prefix in all chapter names (Event tag).
     *
     * @param pgnGames The list of PgnGames to convert.
     * @return The study name.
     */
    String getStudyName(final List<PgnGame> pgnGames) {

        final StringBuilder studyName = new StringBuilder();

        final List<String> chapterNames = pgnGames
            .stream()
            .map(PgnGame::getTags)
            .filter(Objects::nonNull)
            .map(tags -> tags.get("Event"))
            .filter(Objects::nonNull)
            .toList();

        if (chapterNames.isEmpty())
            return null;

        final int minChapterNameLength = chapterNames
            .stream()
            .map(String::length)
            .sorted()
            .findAny()
            .orElse(0);

        for (int i = 0; i < minChapterNameLength; i++) {
            final char c = chapterNames.getFirst().charAt(i);
            boolean commonChar = true;
            for (int j = 1; j < chapterNames.size(); j++) {
                if (chapterNames.get(j).charAt(i) != c) {
                    commonChar = false;
                    break;
                }
            }
            if (commonChar) studyName.append(c);
            else break;
        }

        final int lastColumnIndex = studyName.toString().lastIndexOf(": ");
        return lastColumnIndex != -1
            ? studyName.substring(0, lastColumnIndex)
            : studyName.toString();
    }

    /**
     * Build the chapters from a list of PgnGames.
     *
     * @param pgnGames  is the list of PgnGames to convert in Chapters.
     * @param studyName is the name of the study, that leads each chapter name
     *                  and that we will remove.
     * @return a list of Chapters.
     */
    List<Chapter> buildChapters(final List<PgnGame> pgnGames,
                                final String studyName) {

        return pgnGames
            .stream()
            .map(pgnGame -> Chapter.builder()
                .id(UUID.randomUUID())
                .title(this.cleanChapterName(
                    Optional
                        .ofNullable(pgnGame.getTags())
                        .orElse(Map.of())
                        .get("Event"),
                    studyName))
                .nextMoves(Optional
                    .ofNullable(pgnGame.getNodes())
                    .orElse(List.of())
                    .stream()
                    .map(this::buildMove)
                    .toList())
                .build())
            .toList();
    }

    /**
     * Remove leading studyName in the chapter title.
     *
     * @param chapterName the source chapter name, prefixed with the study name.
     * @param studyName   the study name to find and remove from the chapter
     *                    name.
     * @return the cleaned chapter name.
     */
    String cleanChapterName(final String chapterName,
                            final String studyName) {

        return Optional
            .ofNullable(chapterName)
            .orElse("")
            .replaceAll(
                "^" + studyName + "\\s*:\\s*",
                "");
    }

    /**
     * Recursively build a Move from a PgnNode.
     *
     * @param pgnNode is the PgnNode to convert.
     * @return A Move (with the following variations).
     */
    Move buildMove(final PgnNode pgnNode) {

        return Move.builder()
            .id(UUID.randomUUID())
            .san(pgnNode.getSan())
            .uci(pgnNode.getUci())
            .nag(this.buildNag(pgnNode.getNag()))
            .nextMoves(Optional
                .ofNullable(pgnNode.getVariations())
                .orElse(List.of())
                .stream()
                .map(this::buildMove)
                .toList())
            .build();
    }

    /**
     * Convert PgnNag in their Nag equivalent.
     *
     * @param nag is the PgnNag to convert.
     * @return a Nag or null.
     */
    Nag buildNag(final PgnNag nag) {

        if (nag == null)
            return null;

        return switch (nag) {
            case GOOD_MOVE -> Nag.GOOD_MOVE;
            case MISTAKE -> Nag.MISTAKE;
            case BRILLANT_MOVE -> Nag.BRILLANT_MOVE;
            case BLUNDER -> Nag.BLUNDER;
            case SPECULATIVE_MOVE -> Nag.SPECULATIVE_MOVE;
            case DUBIOUS_MOVE -> Nag.DUBIOUS_MOVE;
        };
    }

}
