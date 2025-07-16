package io.github.fifi2.chessmorize.config.properties;

import io.github.fifi2.chessmorize.AbstractSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class TrainingPropertiesTest extends AbstractSpringBootTest {

    @Autowired
    private TrainingProperties trainingProperties;

    @Test
    void getCalendar() {

        final Map<Integer, Long> frequencies = this.trainingProperties.getCalendar()
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

        assertThat(this.trainingProperties.getMaxNumber()).isEqualTo(5);
    }

}
