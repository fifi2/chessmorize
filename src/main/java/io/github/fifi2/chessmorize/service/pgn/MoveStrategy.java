package io.github.fifi2.chessmorize.service.pgn;

import java.util.stream.Stream;

interface MoveStrategy {

    record Vector(int file, int rank) {

        PgnSquare apply(final PgnSquare square) {

            return new PgnSquare(
                square.getFileIdx() + this.file,
                square.getRankIdx() + this.rank);
        }

    }

    Stream<PgnSquare> getCandidateSourceSquares(final PgnSquare target,
                                                final Color color);

}
