package io.github.fifi2.chessmorize.service.pgn;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
enum PgnPieceType {

    PAWN('P', MoveStrategyConstants.PAWN),
    ROOK('R', MoveStrategyConstants.ROOK),
    KNIGHT('N', MoveStrategyConstants.KNIGHT),
    BISHOP('B', MoveStrategyConstants.BISHOP),
    QUEEN('Q', MoveStrategyConstants.QUEEN),
    KING('K', MoveStrategyConstants.KING);

    private static class MoveStrategyConstants {

        private static final MovePawnStrategy PAWN =
            new MovePawnStrategy();
        private static final MoveRookStrategy ROOK =
            new MoveRookStrategy();
        private static final MoveKnightStrategy KNIGHT =
            new MoveKnightStrategy();
        private static final MoveBishopStrategy BISHOP =
            new MoveBishopStrategy();
        private static final MoveQueenStrategy QUEEN =
            new MoveQueenStrategy(BISHOP, ROOK);
        private static final MoveKingStrategy KING =
            new MoveKingStrategy();

    }

    private final Character letter;
    private final MoveStrategy moveStrategy;

    /**
     * Get a PgnPieceType from its alphabetic representation.
     *
     * @param letter The letter (e.g. P, R, N, B, Q or K).
     * @return The associated PgnPieceType.
     */
    static PgnPieceType fromLetter(final String letter) {
        return PgnPieceType.fromLetter(letter.charAt(0));
    }

    /**
     * Get a PgnPieceType from its alphabetic representation.
     *
     * @param letter The letter (e.g. P, R, N, B, Q or K).
     * @return The associated PgnPieceType.
     */
    static PgnPieceType fromLetter(final Character letter) {

        if (letter == null) {
            throw new IllegalArgumentException("letter can't be null");
        }

        return Arrays.stream(PgnPieceType.values())
            .filter(pgnPieceType -> pgnPieceType.getLetter()
                .equals(Character.toUpperCase(letter)))
            .findAny()
            .orElseThrow();
    }

}
