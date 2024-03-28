package io.github.fifi2.chessmorize.service.pgn;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;

/**
 * PgnSquare design a square in a bi-dimensional array with ranks (from 8 to 1)
 * coming first, followed by files (from h to a).
 */

@AllArgsConstructor
@Getter
@EqualsAndHashCode
class PgnSquare {

    private int fileIdx;
    private int rankIdx;

    PgnSquare(final String name) {

        if (name == null) {
            throw new IllegalArgumentException("square name can't be null");
        }

        this.fileIdx = name.charAt(0) - 'a';
        this.rankIdx = Math.abs(Integer.parseInt(name.substring(1, 2)) - 8);
    }

    /**
     * Convert file and rank indices to algebraic notation.
     * E.g. "a1", "b2", etc.
     *
     * @return the square name (e.g. "e4").
     */
    String getName() {

        return this.getFile() + this.getRank();
    }

    /**
     * Get the file name (as a letter from a to h).
     *
     * @return the file letter as a String.
     */
    String getFile() {

        return String.valueOf((char) ('a' + this.fileIdx));
    }

    /**
     * Get the rank name (as a digit from 1 to 8).
     *
     * @return the rank digit as a String.
     */
    String getRank() {

        return String.valueOf((char) ('1' + (7 - this.rankIdx)));
    }

    /**
     * Is the square matching the given disambiguatingMove.
     *
     * @param disambiguatingMove The disambiguating move. E.g. in "Nbd7", the
     *                           disambiguating move part is "b". It can be
     *                           longer such as in Qh4e1 where it is equal to
     *                           "h4".
     * @return a boolean, true if the square is matching, or false.
     */
    boolean isMatchingDisambiguatingMove(String disambiguatingMove) {

        return Optional
            .ofNullable(disambiguatingMove)
            .orElse("")
            .chars()
            .mapToObj(c -> (char) c)
            .allMatch(c -> Character.isDigit(c) && c == this.getRank().charAt(0)
                || Character.isAlphabetic(c) && c == this.getFile().charAt(0));
    }

    /**
     * Check if the PgnSquare indices are valid in a board of 64 squares.
     *
     * @return the validity of the square indices (boolean).
     */
    boolean isValid() {

        return this.fileIdx >= 0 && this.fileIdx <= 7
            && this.rankIdx >= 0 && this.rankIdx <= 7;
    }

    /**
     * Calculate the distance with another square.
     *
     * @param square The distant PgnSquare.
     * @return the distance (int) between the squares.
     */
    int getDistance(PgnSquare square) {

        return Math.max(
            Math.abs(this.fileIdx - square.getFileIdx()),
            Math.abs(this.rankIdx - square.getRankIdx()));
    }

}
