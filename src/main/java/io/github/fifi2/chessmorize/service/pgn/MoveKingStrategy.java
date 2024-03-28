package io.github.fifi2.chessmorize.service.pgn;

import java.util.stream.IntStream;
import java.util.stream.Stream;

class MoveKingStrategy implements MoveStrategy {

    @Override
    public Stream<PgnSquare> getCandidateSourceSquares(final PgnSquare target,
                                                       final Color color) {

        return IntStream
            .range(-1, 2)
            .boxed()
            .flatMap(fileStep -> IntStream.range(-1, 2)
                .boxed()
                .filter(rankStep -> !fileStep.equals(0)
                    || !rankStep.equals(0))
                .map(rankStep -> new Vector(fileStep, rankStep))
                .map(vector -> vector.apply(target)))
            .filter(PgnSquare::isValid);
    }

}
