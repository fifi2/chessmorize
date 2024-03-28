package io.github.fifi2.chessmorize.helper.converter;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import java.util.Arrays;
import java.util.List;

public class StringToList implements ArgumentConverter {

    private static final String COMMA = ",";

    @Override
    public Object convert(final Object source,
                          final ParameterContext parameterContext)
        throws ArgumentConversionException {

        if (source == null) {
            return List.of();
        }

        if (!(source instanceof String input)) {
            throw new IllegalArgumentException("The source is not a string");
        }

        return Arrays.asList(input.split(COMMA));
    }

}
