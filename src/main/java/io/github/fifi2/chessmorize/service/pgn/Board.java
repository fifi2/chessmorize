package io.github.fifi2.chessmorize.service.pgn;

import lombok.Data;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.github.fifi2.chessmorize.service.pgn.Color.*;
import static io.github.fifi2.chessmorize.service.pgn.PgnPieceType.*;

@Data
class Board {

    static final String FEN_START = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    static final String FEN_NULL = "-";

    private static final Pattern PATTERN_FEN = Pattern.compile("^"
        + "(?<position>\\S+) "
        + "(?<turn>[wb]) "
        + "(?<castlingAvailability>[KQkq\\-]+) "
        + "(?<enPassantTarget>\\S+) "
        + "(?<halfMoveClock>\\d+) "
        + "(?<fullMoveNumber>\\d+)"
        + "$");
    private static final String FEN_FIELDS_SPLIT = " ";
    private static final String FEN_POSITION_SPLIT = "/";
    private static final String FEN_WHITE_TURN = "w";
    private static final String FEN_BLACK_TURN = "b";
    private static final String SAN_CAPTURE_FLAG = "x";
    private static final Pattern PATTERN_SAN = Pattern.compile("^"
        + "(?<piece>[RNBQK])?"         // piece letter or null for a pawn move
        + "(?<clue>[a-h1-8]){0,2}"     // disambiguating part (e.g. Nbd7 or Qh4e1)
        + "(?<capture>x)?"             // capture flag "x"
        + "(?<target>[a-h][1-8])"      // arrival square
        + "(?:=(?<promotion>[RNBQ]))?" // promotion
        + ".*$");                      // optional trailing data

    private static final PgnSquare SQUARE_ROOK_A1 = new PgnSquare("a1");
    private static final PgnSquare SQUARE_ROOK_H1 = new PgnSquare("h1");
    private static final PgnSquare SQUARE_ROOK_A8 = new PgnSquare("a8");
    private static final PgnSquare SQUARE_ROOK_H8 = new PgnSquare("h8");

    private Piece[][] position;
    private Color turn;
    private String castlingAvailability;
    private String enPassantTargetSquare;
    private Integer halfMoveClock;
    private Integer fullMoveNumber;
    private String uci;

    record Piece(PgnPieceType type, Color color) {

        /**
         * Get the FEN notation for the piece (e.g. P, p, R, r, etc.)
         * (the FEN notation depends on both type and color of the piece).
         *
         * @return The FEN notation as a Character.
         */
        Character getFenNotation() {

            final Character letter = this.type.getLetter();
            return this.color == Color.WHITE
                ? letter
                : Character.toLowerCase(letter);
        }

    }

    record Move(PgnPieceType pieceType,
                PgnSquare source,
                boolean capture,
                PgnSquare target,
                Piece promotion,
                PgnSquare enPassantTarget,
                Move castlingMove) {
    }

    Board() {
        this(FEN_START);
    }

    Board(final String fen) {

        this.position = new Piece[8][8];

        PATTERN_FEN.matcher(fen)
            .results()
            .findAny()
            .ifPresentOrElse(
                result -> {
                    this.setFenPosition(result.group("position"));
                    this.turn = fromFenNotation(result.group("turn"));
                    this.castlingAvailability = FEN_NULL.equals(
                        result.group("castlingAvailability"))
                        ? null
                        : result.group("castlingAvailability");
                    this.enPassantTargetSquare =
                        FEN_NULL.equals(result.group("enPassantTarget"))
                            ? null
                            : result.group("enPassantTarget");
                    this.halfMoveClock = Integer.valueOf(
                        result.group("halfMoveClock"));
                    this.fullMoveNumber = Integer.valueOf(
                        result.group("fullMoveNumber"));
                },
                () -> {
                    throw new IllegalArgumentException("invalid FEN");
                }
            );
    }

    private void setFenPosition(final String fenPosition) {

        final String[] positionRows = fenPosition.split(FEN_POSITION_SPLIT);

        for (int row = 0; row < positionRows.length; row++) {
            final String fenRow = positionRows[row];
            int column = 0;
            for (int rowIdx = 0; rowIdx < fenRow.length(); rowIdx++) {
                final char fenChar = fenRow.charAt(rowIdx);
                if (Character.isDigit(fenChar)) {
                    // set empty squares to null
                    int numEmptySquares = Character.getNumericValue(fenChar);
                    for (int j = 0; j < numEmptySquares; j++) {
                        position[row][column++] = null;
                    }
                } else {
                    position[row][column++] = new Piece(
                        fromLetter(fenChar),
                        Character.isUpperCase(fenChar)
                            ? WHITE
                            : BLACK);
                }
            }
        }
    }

    String getFen() {

        return String.join(
            FEN_FIELDS_SPLIT,
            List.of(
                this.getFenPosition(),
                String.valueOf(this.turn.getFenNotation()),
                Optional.ofNullable(this.castlingAvailability)
                    .orElse(FEN_NULL),
                Optional.ofNullable(this.enPassantTargetSquare)
                    .orElse(FEN_NULL),
                String.valueOf(halfMoveClock),
                String.valueOf(fullMoveNumber)));
    }

    private String getFenPosition() {

        final StringBuilder positionBuilder = new StringBuilder();

        for (int row = 0; row < this.position.length; row++) {
            int emptySquareCount = 0;
            for (int col = 0; col < this.position[row].length; col++) {
                final Piece piece = this.position[row][col];
                if (piece == null)
                    emptySquareCount++;
                else {
                    if (emptySquareCount > 0) {
                        positionBuilder.append(emptySquareCount);
                        emptySquareCount = 0;
                    }
                    positionBuilder.append(piece.getFenNotation());
                }
            }
            if (emptySquareCount > 0)
                positionBuilder.append(emptySquareCount);
            if (row < position.length - 1)
                positionBuilder.append(FEN_POSITION_SPLIT);
        }

        return positionBuilder.toString();
    }

    void move(final String san) {

        Optional
            .ofNullable(parseSan(san, this.turn))
            .ifPresent(m -> {
                this.updateCastlingAvailability(m);
                this.halfMoveClock = m.pieceType() == PAWN || m.capture()
                    ? 0
                    : this.halfMoveClock + 1;

                this.getPiece(m.source()).ifPresentOrElse(
                    piece -> {
                        this.setPiece(
                            Optional.ofNullable(m.promotion()).orElse(piece),
                            m.target());
                        this.setPiece(null, m.source());

                        // Handle en passant
                        Optional
                            .ofNullable(this.enPassantTargetSquare)
                            .filter(dummy -> piece.type() == PAWN)
                            .map(PgnSquare::new)
                            .filter(s -> s.equals(m.target()))
                            .map(s -> new PgnSquare(
                                s.getFileIdx(),
                                s.getRankIdx() + (this.turn == WHITE ? 1 : -1)))
                            .ifPresent(s -> this.setPiece(null, s));

                        // Handle castling
                        Optional
                            .ofNullable(m.castlingMove())
                            .ifPresent(rookMove ->
                                this.getPiece(rookMove.source())
                                    .ifPresentOrElse(
                                        rook -> {
                                            this.setPiece(
                                                rook,
                                                rookMove.target());
                                            this.setPiece(
                                                null,
                                                rookMove.source());
                                        },
                                        () -> {
                                            throw new NoSuchElementException(
                                                "no rook found");
                                        }));

                        this.uci = Board.buildUci(m);
                    },
                    () -> {
                        throw new NoSuchElementException("no piece found");
                    });

                this.enPassantTargetSquare = Optional
                    .ofNullable(m.enPassantTarget())
                    .map(PgnSquare::getName)
                    .orElse(null);

                if (this.turn == WHITE) {
                    this.turn = BLACK;
                } else {
                    this.turn = WHITE;
                    this.fullMoveNumber++;
                }
            });
    }

    /**
     * Extract the move information from a SAN
     * to be able to play it on the board.
     * If O-O / O-O-O then castling
     * - short or long
     * - takes color to complete Move
     * If starting with [RNBQK] then piece move
     * - Qh4xe1
     * - might have "disambiguating moves" part (e.g. h, 4 or even h4)
     * - might have a capture flag (e.g. x)
     * - must have a target square (e.g. e1)
     * Else it's a pawn move
     * - cxd4 / c6 / e8=Q / exd6 e.p.
     *
     * @param san   The SAN of the move.
     * @param color The color of the moved piece.
     * @return A Move.
     */
    Move parseSan(final String san, final Color color) {

        if (san.startsWith("O-O")) {
            // castling
            final boolean shortCastle = san.equals("O-O");
            return new Move(
                KING,
                new PgnSquare(color == WHITE ? "e1" : "e8"),
                false,
                Board.getKingTargetCastlingSquare(color, shortCastle),
                null,
                null,
                new Move(
                    ROOK,
                    Board.getRookSourceCastlingSquare(color, shortCastle),
                    false,
                    Board.getRookTargetCastlingSquare(color, shortCastle),
                    null,
                    null,
                    null));
        }

        return Optional.of(PATTERN_SAN.matcher(san))
            .filter(Matcher::find)
            .map(matcher -> {
                final PgnPieceType pieceType = Optional
                    .ofNullable(matcher.group("piece"))
                    .map(PgnPieceType::fromLetter)
                    .orElse(PAWN);
                final PgnSquare target = new PgnSquare(matcher.group("target"));
                final boolean isCapture = SAN_CAPTURE_FLAG.equals(matcher.group("capture"));
                return new Move(
                    pieceType,
                    this.getSourceSquare(
                        pieceType,
                        color,
                        target,
                        matcher.group("clue"),
                        isCapture),
                    isCapture,
                    target,
                    Optional.ofNullable(matcher.group("promotion"))
                        .map(PgnPieceType::fromLetter)
                        .map(pgnPieceType -> new Piece(pgnPieceType, color))
                        .orElse(null),
                    this.buildEnPassantTargetSquare(pieceType, color, target),
                    null);
            })
            .orElse(null);
    }

    void updateCastlingAvailability(Move move) {

        final Set<String> availabilitiesToRemove = new HashSet<>();

        if (this.castlingAvailability == null) {
            return;
        }

        if (move.pieceType() == KING) {
            availabilitiesToRemove.add("K");
            availabilitiesToRemove.add("Q");
        } else if (move.pieceType() == ROOK) {
            final PgnSquare moveSource = move.source();
            if (SQUARE_ROOK_A1.equals(moveSource) && this.turn == WHITE
                || SQUARE_ROOK_A8.equals(moveSource) && this.turn == BLACK)
                availabilitiesToRemove.add("Q");
            else if (SQUARE_ROOK_H1.equals(moveSource) && this.turn == WHITE
                || SQUARE_ROOK_H8.equals(moveSource) && this.turn == BLACK)
                availabilitiesToRemove.add("K");
        }

        final String newAvailability = availabilitiesToRemove
            .stream()
            .map(availability -> this.turn == WHITE
                ? availability
                : availability.toLowerCase())
            .reduce(
                this.castlingAvailability,
                (availability, removal) -> availability.replace(
                    removal,
                    ""));

        this.castlingAvailability = newAvailability.isEmpty()
            ? null
            : newAvailability;
    }

    /**
     * Get the UCI move.
     * E.g.:
     * - e2e4
     * - e1g1 (white short castling)
     * - e7e8q (for promotion)
     *
     * @param m The Move to translate.
     * @return The UCI move as a String.
     */
    static String buildUci(Move m) {

        final String promotionChar = Optional
            .ofNullable(m.promotion())
            .map(Piece::type)
            .map(PgnPieceType::getLetter)
            .map(String::valueOf)
            .map(String::toLowerCase)
            .orElse("");

        return m.source().getName() + m.target().getName() + promotionChar;
    }

    static PgnSquare getKingTargetCastlingSquare(final Color color,
                                                 boolean shortCastle) {

        if (color == WHITE) {
            return shortCastle
                ? new PgnSquare("g1")
                : new PgnSquare("c1");
        }
        return shortCastle
            ? new PgnSquare("g8")
            : new PgnSquare("c8");
    }

    static PgnSquare getRookTargetCastlingSquare(final Color color,
                                                 boolean shortCastle) {

        if (color == WHITE) {
            return shortCastle
                ? new PgnSquare("f1")
                : new PgnSquare("d1");
        }
        return shortCastle
            ? new PgnSquare("f8")
            : new PgnSquare("d8");
    }

    static PgnSquare getRookSourceCastlingSquare(final Color color,
                                                 boolean shortCastle) {

        if (color == WHITE) {
            return shortCastle
                ? new PgnSquare("h1")
                : new PgnSquare("a1");
        }
        return shortCastle
            ? new PgnSquare("h8")
            : new PgnSquare("a8");
    }

    private PgnSquare getSourceSquare(final PgnPieceType pieceType,
                                      final Color color,
                                      final PgnSquare target,
                                      final String clue,
                                      final boolean isCapture) {

        return pieceType.getMoveStrategy()
            .getCandidateSourceSquares(target, color)
            // we keep a square only if we can find the expected piece on it
            .filter(square -> this.getPiece(square)
                .filter(piece -> piece.type() == pieceType)
                .filter(piece -> piece.color() == color)
                .isPresent())
            // use capture flag as a disambiguating information for pawn moves
            .filter(square -> pieceType != PAWN
                || isCapture == (square.getFileIdx() != target.getFileIdx()))
            // if we have some disambiguating information, we filter our
            // candidate squares with this clue
            .filter(square -> square.isMatchingDisambiguatingMove(clue))
            // we keep the closest square
            // (to handle a position with Qa1, Qa2 and target = c3 for example)
            .reduce((nearestSquare, square) ->
                target.getDistance(square) < target.getDistance(nearestSquare)
                    ? square
                    : nearestSquare)
            .orElse(null);
    }

    private PgnSquare buildEnPassantTargetSquare(final PgnPieceType pieceType,
                                                 final Color color,
                                                 final PgnSquare target) {

        return Stream.of(-1, 1)
            .filter(dummy -> pieceType == PAWN)
            .filter(dummy -> color == WHITE && target.getRankIdx() == 6
                || color == BLACK && target.getRankIdx() == 3)
            .map(file -> new PgnSquare(
                target.getFileIdx() + file,
                target.getRankIdx()))
            .filter(PgnSquare::isValid)
            .map(this::getPiece)
            .map(optionalPiece -> optionalPiece.orElse(null))
            .filter(Objects::nonNull)
            .filter(piece -> piece.type() == PAWN && piece.color() != color)
            // TODO check if the capture is legal
            .findAny()
            .map(dummy -> new PgnSquare(
                target.getFileIdx(),
                color == WHITE ? 5 : 2))
            .orElse(null);
    }

    /**
     * Set a piece on a named square.
     *
     * @param piece  The piece to put on the square
     *               (null to remove any present piece on the square).
     * @param square The square.
     */
    void setPiece(final Piece piece, final PgnSquare square) {

        this.position[square.getRankIdx()][square.getFileIdx()] = piece;
    }

    /**
     * Get a piece from a named square.
     *
     * @param square The square where to get the piece.
     * @return an optional describing the Piece (empty if there was no piece
     * on the square).
     */
    Optional<Piece> getPiece(final PgnSquare square) {

        return Optional
            .ofNullable(square)
            .map(s -> this.position[square.getRankIdx()][square.getFileIdx()]);
    }

}
