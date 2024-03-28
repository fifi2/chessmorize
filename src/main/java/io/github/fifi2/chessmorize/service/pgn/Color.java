package io.github.fifi2.chessmorize.service.pgn;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
enum Color {

    WHITE('w'),
    BLACK('b');

    private final Character fenNotation;

    static Color fromFenNotation(String fenNotation) {

        return Color.fromFenNotation(fenNotation.charAt(0));
    }

    static Color fromFenNotation(Character fenNotation) {

        if (fenNotation == null) {
            throw new IllegalArgumentException("fenNotation can't be null");
        }

        return Arrays.stream(Color.values())
            .filter(color -> color.fenNotation.equals(fenNotation))
            .findAny()
            .orElseThrow();
    }

    static Color fromName(String colorName) {

        return Arrays.stream(Color.values())
            .filter(color -> color.name().equals(colorName))
            .findAny()
            .orElseThrow();
    }

}
