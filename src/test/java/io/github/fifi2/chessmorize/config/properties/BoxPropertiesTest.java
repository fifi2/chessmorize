package io.github.fifi2.chessmorize.config.properties;

import io.github.fifi2.chessmorize.helper.AbstractSpringBootTest;
import io.github.fifi2.chessmorize.config.properties.BoxProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class BoxPropertiesTest extends AbstractSpringBootTest {

    @Autowired
    private BoxProperties boxProperties;

    @Test
    void getCalendar() {
        final Map<Integer, Long> frequencies = this.boxProperties.getCalendar()
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.groupingBy(
                Integer::intValue,
                Collectors.counting()));

        assertThat(frequencies).containsExactlyInAnyOrderEntriesOf(Map.of(
            0, 32L,
            1, 16L,
            2, 8L,
            3, 4L,
            4, 2L,
            5, 1L
        ));
    }

    @Test
    void getMaxNumber() {

        assertThat(this.boxProperties.getMaxNumber()).isEqualTo(5);
    }

}
