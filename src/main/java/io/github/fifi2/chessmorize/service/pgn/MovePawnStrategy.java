package io.github.fifi2.chessmorize.service.pgn;

import java.util.stream.Stream;

import static io.github.fifi2.chessmorize.service.pgn.Color.BLACK;
import static io.github.fifi2.chessmorize.service.pgn.Color.WHITE;

class MovePawnStrategy implements MoveStrategy {

    @Override
    public Stream<PgnSquare> getCandidateSourceSquares(final PgnSquare target,
                                                       final Color color) {

        final int step = color == WHITE ? 1 : -1;

        return Stream.concat(
                Stream.of(
                    new Vector(0, step),  // push
                    new Vector(-1, step), // takes
                    new Vector(1, step)), // takes
                color == WHITE && target.getRankIdx() == 4
                    || color == BLACK && target.getRankIdx() == 3
                    ? Stream.of(new Vector(0, step * 2)) // first push
                    : Stream.empty())
            .map(vector -> vector.apply(target))
            .filter(PgnSquare::isValid);
    }

}
