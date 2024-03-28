package io.github.fifi2.chessmorize.service.pgn;

import io.github.fifi2.chessmorize.service.pgn.PgnNode;

@FunctionalInterface
interface NodeAssertion {

    void runAssert(PgnNode node);

}
