package io.github.fifi2.chessmorize.converter;

import io.github.fifi2.chessmorize.model.*;
import io.github.fifi2.chessmorize.service.pgn.PgnGame;
import io.github.fifi2.chessmorize.service.pgn.PgnNag;
import io.github.fifi2.chessmorize.service.pgn.PgnNode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class PgnGamesToBookConverter {

    /**
     * Convert some PgnGames, from Lichess PGN parsing, into a Book.
     *
     * @param pgnGames The list of PgnGames to convert.
     * @param color    is the player color.
     * @return A Book.
     */
    public Book convert(final List<PgnGame> pgnGames,
                        final String studyId,
                        final Color color) {

        return Book.builder()
            .id(UUID.randomUUID())
            .studyId(studyId)
            .name(Optional
                .ofNullable(pgnGames.getFirst().getTags())
                .orElse(Map.of())
                .get("StudyName"))
            .color(color)
            .chapters(this.buildChapters(pgnGames))
            .build();
    }

    /**
     * Build the chapters from a list of PgnGames.
     *
     * @param pgnGames is the list of PgnGames to convert in Chapters.
     * @return a list of Chapters.
     */
    List<Chapter> buildChapters(final List<PgnGame> pgnGames) {

        return pgnGames
            .stream()
            .map(pgnGame -> Chapter.builder()
                .id(UUID.randomUUID())
                .title(Optional
                    .ofNullable(pgnGame.getTags())
                    .orElse(Map.of())
                    .get("ChapterName"))
                .enabled(true)
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
            .comment(pgnNode.getComment())
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
     * @param pgnNag is the PgnNag to convert.
     * @return a Nag or null.
     */
    Nag buildNag(final PgnNag pgnNag) {

        if (pgnNag == null)
            return null;

        try {
            return Nag.valueOf(pgnNag.name());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
