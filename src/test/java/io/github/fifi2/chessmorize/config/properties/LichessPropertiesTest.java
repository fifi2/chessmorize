package io.github.fifi2.chessmorize.config.properties;

import io.github.fifi2.chessmorize.helper.AbstractSpringBootTest;
import io.github.fifi2.chessmorize.config.properties.LichessProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class LichessPropertiesTest extends AbstractSpringBootTest {

    @Autowired
    private LichessProperties lichessProperties;

    @Test
    void getUrl() {

        assertThat(this.lichessProperties.getUrl())
            .withFailMessage("no url in lichess properties")
            .isNotBlank()
            .startsWith("http");
    }

}
