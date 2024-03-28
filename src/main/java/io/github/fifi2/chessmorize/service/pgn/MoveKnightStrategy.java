package io.github.fifi2.chessmorize.service.pgn;

import java.util.stream.Stream;

class MoveKnightStrategy implements MoveStrategy {

    @Override
    public Stream<PgnSquare> getCandidateSourceSquares(final PgnSquare target,
                                                       final Color color) {

        return Stream.of(
                new Vector(1, 2),
                new Vector(2, 1))
            .flatMap(a -> Stream.of(
                    new Vector(1, 1),
                    new Vector(-1, -1),
                    new Vector(1, -1),
                    new Vector(-1, 1))
                .map(b -> new Vector(
                    a.file() * b.file(),
                    a.rank() * b.rank()))
                .map(vector -> vector.apply(target)))
            .filter(PgnSquare::isValid);
    }

}
