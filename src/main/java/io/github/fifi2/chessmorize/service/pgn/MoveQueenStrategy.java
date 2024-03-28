package io.github.fifi2.chessmorize.service.pgn;

import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
class MoveQueenStrategy implements MoveStrategy {

    private final MoveBishopStrategy bishopStrategy;
    private final MoveRookStrategy rookStrategy;

    @Override
    public Stream<PgnSquare> getCandidateSourceSquares(final PgnSquare target,
                                                       final Color color) {

        return Stream.concat(
            bishopStrategy.getCandidateSourceSquares(target, color),
            rookStrategy.getCandidateSourceSquares(target, color)
        );
    }

}
