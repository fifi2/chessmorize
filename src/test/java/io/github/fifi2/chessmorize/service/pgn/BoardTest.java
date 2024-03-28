package io.github.fifi2.chessmorize.service.pgn;

import io.github.fifi2.chessmorize.service.pgn.Board;
import io.github.fifi2.chessmorize.service.pgn.Color;
import io.github.fifi2.chessmorize.service.pgn.PgnPieceType;
import io.github.fifi2.chessmorize.service.pgn.PgnSquare;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static io.github.fifi2.chessmorize.service.pgn.Board.*;
import static io.github.fifi2.chessmorize.service.pgn.Color.BLACK;
import static io.github.fifi2.chessmorize.service.pgn.Color.WHITE;
import static io.github.fifi2.chessmorize.service.pgn.PgnPieceType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class BoardTest {

    private record PositionAssertSpec(int file,
                                      int rank,
                                      PgnPieceType pieceType,
                                      Color color) {
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        PAWN   | WHITE | P
        ROOK   | WHITE | R
        KNIGHT | WHITE | N
        BISHOP | WHITE | B
        QUEEN  | WHITE | Q
        KING   | WHITE | K
        PAWN   | BLACK | p
        ROOK   | BLACK | r
        KNIGHT | BLACK | n
        BISHOP | BLACK | b
        QUEEN  | BLACK | q
        KING   | BLACK | k
        """)
    void getFenNotation(final PgnPieceType pgnPieceType,
                        final Color color,
                        final Character expectedFenNotation) {

        final Piece piece = new Piece(pgnPieceType, color);
        assertThat(piece.getFenNotation()).isEqualTo(expectedFenNotation);
    }

    @Test
    void setFen_viaDefaultConstructor() {

        final Board board = new Board();
        assertThat(board.getTurn()).isEqualTo(WHITE);
        assertThat(board.getCastlingAvailability()).isEqualTo("KQkq");
        assertThat(board.getEnPassantTargetSquare()).isNull();
        assertThat(board.getHalfMoveClock()).isZero();
        assertThat(board.getFullMoveNumber()).isEqualTo(1);

        assertPosition(board.getPosition(), Stream.of(
            new PositionAssertSpec(0, 0, ROOK, BLACK),
            new PositionAssertSpec(1, 0, KNIGHT, BLACK),
            new PositionAssertSpec(2, 0, BISHOP, BLACK),
            new PositionAssertSpec(3, 0, QUEEN, BLACK),
            new PositionAssertSpec(4, 0, KING, BLACK),
            new PositionAssertSpec(5, 0, BISHOP, BLACK),
            new PositionAssertSpec(6, 0, KNIGHT, BLACK),
            new PositionAssertSpec(7, 0, ROOK, BLACK),
            new PositionAssertSpec(0, 1, PAWN, BLACK),
            new PositionAssertSpec(1, 1, PAWN, BLACK),
            new PositionAssertSpec(2, 1, PAWN, BLACK),
            new PositionAssertSpec(3, 1, PAWN, BLACK),
            new PositionAssertSpec(4, 1, PAWN, BLACK),
            new PositionAssertSpec(5, 1, PAWN, BLACK),
            new PositionAssertSpec(6, 1, PAWN, BLACK),
            new PositionAssertSpec(7, 1, PAWN, BLACK),
            new PositionAssertSpec(0, 6, PAWN, WHITE),
            new PositionAssertSpec(1, 6, PAWN, WHITE),
            new PositionAssertSpec(2, 6, PAWN, WHITE),
            new PositionAssertSpec(3, 6, PAWN, WHITE),
            new PositionAssertSpec(4, 6, PAWN, WHITE),
            new PositionAssertSpec(5, 6, PAWN, WHITE),
            new PositionAssertSpec(6, 6, PAWN, WHITE),
            new PositionAssertSpec(7, 6, PAWN, WHITE),
            new PositionAssertSpec(0, 7, ROOK, WHITE),
            new PositionAssertSpec(1, 7, KNIGHT, WHITE),
            new PositionAssertSpec(2, 7, BISHOP, WHITE),
            new PositionAssertSpec(3, 7, QUEEN, WHITE),
            new PositionAssertSpec(4, 7, KING, WHITE),
            new PositionAssertSpec(5, 7, BISHOP, WHITE),
            new PositionAssertSpec(6, 7, KNIGHT, WHITE),
            new PositionAssertSpec(7, 7, ROOK, WHITE)));
    }

    @Test
    void setFen_viaConstructor() {

        final Board board = new Board("rn1qkb1r/pp2pppp/2p2n2/3p1b2/2PP4/2N2N2/PP2PPPP/R1BQKB1R w KQkq - 4 5");
        assertThat(board.getTurn()).isEqualTo(WHITE);
        assertThat(board.getCastlingAvailability()).isEqualTo("KQkq");
        assertThat(board.getEnPassantTargetSquare()).isNull();
        assertThat(board.getHalfMoveClock()).isEqualTo(4);
        assertThat(board.getFullMoveNumber()).isEqualTo(5);

        assertPosition(board.getPosition(), Stream.of(
            new PositionAssertSpec(0, 0, ROOK, BLACK),
            new PositionAssertSpec(1, 0, KNIGHT, BLACK),
            new PositionAssertSpec(3, 0, QUEEN, BLACK),
            new PositionAssertSpec(4, 0, KING, BLACK),
            new PositionAssertSpec(5, 0, BISHOP, BLACK),
            new PositionAssertSpec(7, 0, ROOK, BLACK),
            new PositionAssertSpec(0, 1, PAWN, BLACK),
            new PositionAssertSpec(1, 1, PAWN, BLACK),
            new PositionAssertSpec(4, 1, PAWN, BLACK),
            new PositionAssertSpec(5, 1, PAWN, BLACK),
            new PositionAssertSpec(6, 1, PAWN, BLACK),
            new PositionAssertSpec(7, 1, PAWN, BLACK),
            new PositionAssertSpec(2, 2, PAWN, BLACK),
            new PositionAssertSpec(5, 2, KNIGHT, BLACK),
            new PositionAssertSpec(3, 3, PAWN, BLACK),
            new PositionAssertSpec(5, 3, BISHOP, BLACK),
            new PositionAssertSpec(2, 4, PAWN, WHITE),
            new PositionAssertSpec(3, 4, PAWN, WHITE),
            new PositionAssertSpec(2, 5, KNIGHT, WHITE),
            new PositionAssertSpec(5, 5, KNIGHT, WHITE),
            new PositionAssertSpec(0, 6, PAWN, WHITE),
            new PositionAssertSpec(1, 6, PAWN, WHITE),
            new PositionAssertSpec(4, 6, PAWN, WHITE),
            new PositionAssertSpec(5, 6, PAWN, WHITE),
            new PositionAssertSpec(6, 6, PAWN, WHITE),
            new PositionAssertSpec(7, 6, PAWN, WHITE),
            new PositionAssertSpec(0, 7, ROOK, WHITE),
            new PositionAssertSpec(2, 7, BISHOP, WHITE),
            new PositionAssertSpec(3, 7, QUEEN, WHITE),
            new PositionAssertSpec(4, 7, KING, WHITE),
            new PositionAssertSpec(5, 7, BISHOP, WHITE),
            new PositionAssertSpec(7, 7, ROOK, WHITE)));
    }

    @Test
    void setFen_viaConstructorWithInvalidFen() {

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> new Board("invalid"));
    }

    @Test
    void getFen_default() {

        final Board board = new Board();
        assertThat(board.getFen()).isEqualTo(FEN_START);
    }

    @DisplayName("Given a board, able to do the following moves:")
    @ParameterizedTest(name = "{index}: {3}")
    @CsvSource(delimiter = '|', textBlock = """
        rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1            | e4      | rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1           | e2e4  | A simple pawn push, on a brand new board
        rnbqkbnr/pp1ppppp/2p5/8/3PP3/8/PPP2PPP/RNBQKBNR b KQkq - 0 2        | d5      | rnbqkbnr/pp2pppp/2p5/3p4/3PP3/8/PPP2PPP/RNBQKBNR w KQkq - 0 3        | d7d5  | A pawn push (not the invalid "pawn takes" move)
        rnbqkbnr/pp2pppp/2p5/3p4/3PP3/8/PPP2PPP/RNBQKBNR w KQkq - 0 3       | exd5    | rnbqkbnr/pp2pppp/2p5/3P4/3P4/8/PPP2PPP/RNBQKBNR b KQkq - 0 3         | e4d5  | A capture with a pawn
        rn1qkbnr/pp3ppp/4p1b1/2ppP3/3P2PP/2N5/PPP1NP2/R1BQKB1R b KQkq - 0 7 | h5      | rn1qkbnr/pp3pp1/4p1b1/2ppP2p/3P2PP/2N5/PPP1NP2/R1BQKB1R w KQkq - 0 8 | h7h5  | A pawn long move, without en passant target
        rnbqkbnr/1ppppppp/p7/4P3/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 2         | d5      | rnbqkbnr/1pp1pppp/p7/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3        | d7d5 | A move giving an en passant target square
        rnbqkbnr/1ppppppp/8/2P1P3/p7/8/PP1P1PPP/RNBQKBNR b KQkq - 0 4       | d5      | rnbqkbnr/1pp1pppp/8/2PpP3/p7/8/PP1P1PPP/RNBQKBNR w KQkq d6 0 5       | d7d5 | A move giving an en passant target square from two squares
        rnbqkbnr/1pp1pppp/p7/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3       | exd6    | rnbqkbnr/1pp1pppp/p2P4/8/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 3          | e5d6  | En passant
        rnbqkbnr/1pp1pppp/8/2PpP3/p7/8/PP1P1PPP/RNBQKBNR w KQkq d6 0 5      | cxd6    | rnbqkbnr/1pp1pppp/3P4/4P3/p7/8/PP1P1PPP/RNBQKBNR b KQkq - 0 5        | c5d6  | En passant with multiple choices
        rnbqk1nr/ppp2ppp/8/4P3/1BP5/8/PP2KpPP/RN1Q1BNR b kq - 1 7           | fxg1=N+ | rnbqk1nr/ppp2ppp/8/4P3/1BP5/8/PP2K1PP/RN1Q1BnR w kq - 0 8            | f2g1n | Capture and promotion with check
        6k1/Q7/1Q6/8/3r4/8/6K1/8 w - - 0 40                                 | Qxd4    | 6k1/Q7/8/8/3Q4/8/6K1/8 b - - 0 40                                    | b6d4  | A move where the closest piece should move
        8/8/1k6/8/4Q2Q/8/K7/4r2Q w - - 0 1                                  | Qh4xe1  | 8/8/1k6/8/4Q3/8/K7/4Q2Q b - - 0 1                                    | h4e1  | A move where all disambiguating move matters
        r1bqr1k1/pp1nbppp/2p2n2/3p2B1/3P4/2NBPN2/PPQ2PPP/R3K2R w KQ - 6 10  | O-O     | r1bqr1k1/pp1nbppp/2p2n2/3p2B1/3P4/2NBPN2/PPQ2PPP/R4RK1 b - - 7 10    | e1g1  | Short castle
        r3kbnr/pppq1ppp/2n1b3/4P3/2Pp4/5NP1/PP2PPBP/RNBQ1RK1 b kq - 4 7     | O-O-O   | 2kr1bnr/pppq1ppp/2n1b3/4P3/2Pp4/5NP1/PP2PPBP/RNBQ1RK1 w - - 5 8      | e8c8  | Long castle
        """)
    void move(final String givenFen,
              final String givenMove,
              final String expectedFen,
              final String expectedUci,
              final String description) {

        final Board board = new Board(givenFen);
        board.move(givenMove);
        assertThat(board.getFen()).isEqualTo(expectedFen);
        assertThat(board.getUci()).isEqualTo(expectedUci);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        WHITE | e4     | PAWN   | e4 |      |
        WHITE | dxe4   | PAWN   | e4 |      |
        WHITE | Nf3    | KNIGHT | f3 |      |
        WHITE | Nxf3   | KNIGHT | f3 |      |
        WHITE | Ndf3   | KNIGHT | f3 |      |
        WHITE | Ndxf3  | KNIGHT | f3 |      |
        WHITE | N1f3   | KNIGHT | f3 |      |
        WHITE | N1xf3  | KNIGHT | f3 |      |
        WHITE | Qh4e1  | QUEEN  | e1 |      |
        WHITE | Qh4xe1 | QUEEN  | e1 |      |
        BLACK | Bf5    | BISHOP | f5 |      |
        BLACK | Bxf5   | BISHOP | f5 |      |
        BLACK | Rd1    | ROOK   | d1 |      |
        BLACK | Rxd1   | ROOK   | d1 |      |
        BLACK | Rad1   | ROOK   | d1 |      |
        BLACK | Raxd1  | ROOK   | d1 |      |
        WHITE | O-O    | KING   | g1 | ROOK | f1
        WHITE | O-O-O  | KING   | c1 | ROOK | d1
        BLACK | O-O    | KING   | g8 | ROOK | f8
        BLACK | O-O-O  | KING   | c8 | ROOK | d8
        """)
    void parseSan(final Color color,
                  final String san,
                  final PgnPieceType expectedPiece,
                  final String expectedTarget,
                  final PgnPieceType expectedAdditionalPiece,
                  final String expectedAdditionTarget) {

        final Move move = new Board().parseSan(san, color);
        assertThat(move.pieceType()).isEqualTo(expectedPiece);
        assertThat(move.target())
            .extracting(PgnSquare::getName)
            .isEqualTo(expectedTarget);

        if (expectedAdditionalPiece != null && expectedAdditionTarget != null) {
            assertThat(move.castlingMove())
                .isNotNull()
                .extracting(Move::target)
                .extracting(PgnSquare::getName)
                .isEqualTo(expectedAdditionTarget);
        } else {
            assertThat(move.castlingMove()).isNull();
        }
    }

    @DisplayName("Update castling availability when:")
    @ParameterizedTest(name = "{index}: {0} plays {1} and availabilities were {5}")
    @CsvSource(delimiter = '|', textBlock = """
        WHITE | short castle              | KING | e1 | g1 | KQkq | kq
        WHITE | long castle               | KING | e1 | c1 | KQkq | kq
        WHITE | short castle              | KING | e1 | g1 | KQ   | -
        WHITE | long castle               | KING | e1 | c1 | KQ   | -
        WHITE | its queen side rook       | ROOK | a1 | a2 | KQkq | Kkq
        WHITE | its queen side rook       | ROOK | a1 | a2 | Qkq  | kq
        WHITE | its queen side rook       | ROOK | a1 | a2 | KQ   | K
        WHITE | its queen side rook       | ROOK | a1 | a2 | KQk  | Kk
        WHITE | its queen side rook       | ROOK | a1 | a2 | Q    | -
        WHITE | its queen side rook       | ROOK | a1 | a2 | Qq   | q
        WHITE | its queen side rook later | ROOK | a1 | a2 | kq   | kq
        WHITE | its king side rook        | ROOK | h1 | h2 | KQkq | Qkq
        WHITE | its king side rook        | ROOK | h1 | h2 | Kkq  | kq
        WHITE | its king side rook        | ROOK | h1 | h2 | KQ   | Q
        WHITE | its king side rook        | ROOK | h1 | h2 | KQk  | Qk
        WHITE | its king side rook        | ROOK | h1 | h2 | K    | -
        WHITE | its king side rook        | ROOK | h1 | h2 | Kq   | q
        WHITE | its king side rook later  | ROOK | h1 | h2 | kq   | kq
        BLACK | short castle              | KING | e8 | g8 | KQkq | KQ
        BLACK | long castle               | KING | e8 | c8 | KQkq | KQ
        BLACK | short castle              | KING | e8 | g8 | kq   | -
        BLACK | long castle               | KING | e8 | c8 | kq   | -
        BLACK | its queen side rook       | ROOK | a8 | a7 | KQkq | KQk
        BLACK | its queen side rook       | ROOK | a8 | a7 | KQq  | KQ
        BLACK | its queen side rook       | ROOK | a8 | a7 | kq   | k
        BLACK | its queen side rook       | ROOK | a8 | a7 | Kkq  | Kk
        BLACK | its queen side rook       | ROOK | a8 | a7 | q    | -
        BLACK | its queen side rook       | ROOK | a8 | a7 | Qq   | Q
        BLACK | its queen side rook later | ROOK | a8 | a7 | KQ   | KQ
        BLACK | its king side rook        | ROOK | h8 | h7 | KQkq | KQq
        BLACK | its king side rook        | ROOK | h8 | h7 | KQk  | KQ
        BLACK | its king side rook        | ROOK | h8 | h7 | kq   | q
        BLACK | its king side rook        | ROOK | h8 | h7 | Kkq  | Kq
        BLACK | its king side rook        | ROOK | h8 | h7 | Qk   | Q
        BLACK | its king side rook later  | ROOK | h8 | h7 | KQ   | KQ
        WHITE | something else            | PAWN | e2 | e4 | KQkq | KQkq
        WHITE | something else            | PAWN | e2 | e4 | Qkq  | Qkq
        WHITE | something else            | PAWN | e2 | e4 | Kkq  | Kkq
        WHITE | something else            | PAWN | e2 | e4 | KQq  | KQq
        WHITE | something else            | PAWN | e2 | e4 | KQk  | KQk
        WHITE | something else            | PAWN | e2 | e4 | -    | -
        BLACK | something else            | PAWN | e2 | e4 | KQkq | KQkq
        BLACK | something else            | PAWN | e2 | e4 | Qkq  | Qkq
        BLACK | something else            | PAWN | e2 | e4 | Kkq  | Kkq
        BLACK | something else            | PAWN | e2 | e4 | KQq  | KQq
        BLACK | something else            | PAWN | e2 | e4 | KQk  | KQk
        BLACK | something else            | PAWN | e2 | e4 | -    | -
        """)
    void updateCastlingAvailability(final Color color,
                                    final String description,
                                    final PgnPieceType pieceType,
                                    final String fromSquare,
                                    final String toSquare,
                                    final String initialAvailability,
                                    final String expectedAvailability) {

        final Board board = new Board();

        // fake initial board context
        board.setCastlingAvailability(FEN_NULL.equals(initialAvailability)
            ? null :
            initialAvailability);
        board.setTurn(color);

        final Move move = new Move(
            pieceType,
            new PgnSquare(fromSquare),
            false,
            new PgnSquare(toSquare),
            null,
            null,
            null);
        board.updateCastlingAvailability(move);

        assertThat(board.getCastlingAvailability()).isEqualTo(
            FEN_NULL.equals(expectedAvailability)
                ? null
                : expectedAvailability);
    }

    @DisplayName("Build the UCI move:")
    @ParameterizedTest(name = "{index}: {0} to {2} (capture={1}, promotion={3})")
    @CsvSource(delimiter = '|', textBlock = """
        e2 | false | e4 |        | e2e4
        e1 | false | g1 |        | e1g1
        e7 | false | e8 | QUEEN  | e7e8q
        e7 | true  | d8 | KNIGHT | e7d8n
        """)
    void buildUci(final String from,
                  final boolean capture,
                  final String to,
                  final PgnPieceType promotion,
                  final String expectedUci) {

        final Move move = new Move(
            null,
            new PgnSquare(from),
            capture,
            new PgnSquare(to),
            Optional
                .ofNullable(promotion)
                .map(pieceType -> new Piece(pieceType, null))
                .orElse(null),
            null,
            null);

        assertThat(Board.buildUci(move)).isEqualTo(expectedUci);
    }

    @DisplayName("Get King arrival squares of castling for a:")
    @ParameterizedTest(name = "{index}: {0} king {1} castling")
    @CsvSource(delimiter = '|', textBlock = """
        WHITE | short | g1
        WHITE | long  | c1
        BLACK | short | g8
        BLACK | long  | c8
        """)
    void getKingTargetCastlingSquare(final Color color,
                                     final String castlingType,
                                     final String expectedSquare) {

        final PgnSquare target = Board.getKingTargetCastlingSquare(
            color,
            "short".equals(castlingType));

        assertThat(target)
            .extracting(PgnSquare::getName)
            .isEqualTo(expectedSquare);
    }

    @DisplayName("Get Rook arrival squares of castling for a:")
    @ParameterizedTest(name = "{index}: {0} king {1} castling")
    @CsvSource(delimiter = '|', textBlock = """
        WHITE | short | f1
        WHITE | long  | d1
        BLACK | short | f8
        BLACK | long  | d8
        """)
    void getRookTargetCastlingSquare(final Color color,
                                     final String castlingType,
                                     final String expectedSquare) {

        final PgnSquare target = Board.getRookTargetCastlingSquare(
            color,
            "short".equals(castlingType));

        assertThat(target)
            .extracting(PgnSquare::getName)
            .isEqualTo(expectedSquare);
    }

    @DisplayName("Get Rook source squares of castling for a:")
    @ParameterizedTest(name = "{index}: {0} king {1} castling")
    @CsvSource(delimiter = '|', textBlock = """
        WHITE | short | h1
        WHITE | long  | a1
        BLACK | short | h8
        BLACK | long  | a8
        """)
    void getRookSourceCastlingSquare(final Color color,
                                     final String castlingType,
                                     final String expectedSquare) {

        final PgnSquare target = Board.getRookSourceCastlingSquare(
            color,
            "short".equals(castlingType));

        assertThat(target)
            .extracting(PgnSquare::getName)
            .isEqualTo(expectedSquare);
    }

    @DisplayName("Set a piece on the board, including:")
    @ParameterizedTest(name = "{index}: a {1} {2} on {0}")
    @CsvSource(delimiter = '|', textBlock = """
        f5   | WHITE | ROOK
        """)
    void setPiece(final String squareName,
                  final Color color,
                  final PgnPieceType pieceType) {

        final Board board = new Board();
        final Piece piece = new Piece(pieceType, color);
        final PgnSquare square = new PgnSquare(squareName);
        board.setPiece(piece, square);

        assertThat(board.getPiece(square))
            .isPresent()
            .hasValue(piece);
    }

    @DisplayName("Remove a piece from the board")
    @Test
    void setPiece_removeThePiece() {

        final Board board = new Board();

        // replace an existing piece
        board.setPiece(null, new PgnSquare("a1"));

        // with an already empty square
        board.setPiece(null, new PgnSquare("e4"));

        assertThat(board.getPiece(new PgnSquare("a1")))
            .isNotPresent();
        assertThat(board.getPiece(new PgnSquare("e4")))
            .isNotPresent();
    }

    @DisplayName("Get a piece on the board and:")
    @ParameterizedTest(name = "{index}: find a {1} {2} on {0}")
    @CsvSource(delimiter = '|', textBlock = """
        e2 | WHITE | PAWN
        a8 | BLACK | ROOK
        """)
    void getPiece(final String squareName,
                  final Color expectedColor,
                  final PgnPieceType expectedPieceType) {

        assertThat(new Board().getPiece(new PgnSquare(squareName)))
            .isPresent()
            .hasValue(new Piece(expectedPieceType, expectedColor));
    }

    @DisplayName("Get a piece on the board with a wrong parameter")
    @Test
    void getPiece_withNullSquareName() {

        assertThat(new Board().getPiece(null))
            .isNotPresent();
    }

    private static void assertPosition(
        final Piece[][] position,
        final Stream<PositionAssertSpec> expectedPositionSpecs) {

        final boolean[][] remainingSquares = new boolean[8][8];
        for (boolean[] remainingSquare : remainingSquares)
            Arrays.fill(remainingSquare, true);

        expectedPositionSpecs
            .forEach(spec -> {
                assertThat(position[spec.rank()][spec.file()])
                    .isEqualTo(new Piece(spec.pieceType, spec.color));
                remainingSquares[spec.rank()][spec.file()] = false;
            });

        for (int i = 0; i < remainingSquares.length; i++)
            for (int j = 0; j < remainingSquares[i].length; j++)
                if (remainingSquares[i][j])
                    assertThat(position[i][j]).isNull();
    }

}
