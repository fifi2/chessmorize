package io.github.fifi2.chessmorize.service.pgn;

import io.github.fifi2.chessmorize.helper.converter.StringToList;
import io.github.fifi2.chessmorize.service.pgn.Color;
import io.github.fifi2.chessmorize.service.pgn.PgnPieceType;
import io.github.fifi2.chessmorize.service.pgn.PgnSquare;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoveStrategyTest {

    @DisplayName("Get candidate source squares of a:")
    @ParameterizedTest(name = "{index}: {0} {1} {2}, reaching {3}")
    @CsvSource(delimiter = '|', textBlock = """
        WHITE       | PAWN   | after its first move   | e4 | e2,e3,d3,f3
        WHITE       | PAWN   | in the middle          | e5 | e4,d4,f4
        WHITE       | PAWN   | on the edge (1st move) | h4 | h2,h3,g3
        WHITE       | PAWN   | on the edge            | h5 | g4,h4
        BLACK       | PAWN   | after its first move   | d5 | d7,d6,c6,e6
        BLACK       | PAWN   | in the middle          | g6 | f7,g7,h7
        BLACK       | PAWN   | on the edge (1st move) | h5 | h7,h6,g6
        BLACK       | PAWN   | on the edge            | h4 | g5,h5
        WHITE,BLACK | ROOK   | in the middle          | f5 | a5,b5,c5,d5,e5,g5,h5,f1,f2,f3,f4,f6,f7,f8
        WHITE,BLACK | ROOK   | on the edge            | c8 | a8,b8,d8,e8,f8,g8,h8,c1,c2,c3,c4,c5,c6,c7
        WHITE,BLACK | ROOK   | in the corner          | h1 | a1,b1,c1,d1,e1,f1,g1,h2,h3,h4,h5,h6,h7,h8
        WHITE,BLACK | KNIGHT | in the middle          | f3 | d4,e5,g5,h4,d2,e1,g1,h2
        WHITE,BLACK | KNIGHT | on the edge            | b4 | a6,c6,d5,d3,c2,a2
        WHITE,BLACK | KNIGHT | near the corner        | a2 | b4,c3,c1
        WHITE,BLACK | KNIGHT | in the corner          | a8 | c7,b6
        WHITE,BLACK | BISHOP | in the middle          | c4 | a6,b5,a2,b3,d5,e6,f7,g8,d3,e2,f1
        WHITE,BLACK | BISHOP | on the edge            | b8 | a7,c7,d6,e5,f4,g3,h2
        WHITE,BLACK | BISHOP | in the corner          | a1 | b2,c3,d4,e5,f6,g7,h8
        WHITE,BLACK | QUEEN  | in the middle          | c4 | a4,b4,d4,e4,f4,g4,h4,c1,c2,c3,c5,c6,c7,c8,a2,b3,d5,e6,f7,g8,a6,b5,d3,e2,f1
        WHITE,BLACK | QUEEN  | on the edge            | b8 | a8,c8,d8,e8,f8,g8,h8,b1,b2,b3,b4,b5,b6,b7,c7,d6,e5,f4,g3,h2,a7
        WHITE,BLACK | QUEEN  | in the corner          | a1 | a2,a3,a4,a5,a6,a7,a8,b1,c1,d1,e1,f1,g1,h1,b2,c3,d4,e5,f6,g7,h8
        WHITE,BLACK | KING   | in the middle          | e4 | d5,e5,f5,d4,f4,d3,e3,f3
        WHITE,BLACK | KING   | on the edge            | a2 | a3,b3,b2,a1,b1
        WHITE,BLACK | KING   | in the corner          | h8 | g8,g7,h7
        """)
    void getCandidateSourceSquares(
        @ConvertWith(StringToList.class) final List<String> colors,
        final PgnPieceType pieceType,
        final String description,
        final String target,
        @ConvertWith(StringToList.class) final List<String> expected) {

        colors
            .stream()
            .map(Color::fromName)
            .forEach(color -> {
                final List<String> candidateSourceSquares = pieceType
                    .getMoveStrategy()
                    .getCandidateSourceSquares(new PgnSquare(target), color)
                    .map(PgnSquare::getName)
                    .toList();
                assertThat(candidateSourceSquares)
                    .containsExactlyInAnyOrderElementsOf(expected);
            });
    }

}
