package io.github.fifi2.chessmorize.service.pgn;

import java.util.stream.IntStream;
import java.util.stream.Stream;

class MoveBishopStrategy implements MoveStrategy {

    @Override
    public Stream<PgnSquare> getCandidateSourceSquares(final PgnSquare target,
                                                       final Color color) {

        return IntStream
            .range(-7, 8)
            .boxed()
            .filter(step -> !step.equals(0))
            .flatMap(step -> Stream.of(
                new Vector(step, step),
                new Vector(step, -step),
                new Vector(-step, step),
                new Vector(-step, -step)))
            .distinct()
            .map(vector -> vector.apply(target))
            .filter(PgnSquare::isValid);
    }

}
