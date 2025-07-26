package io.github.fifi2.chessmorize.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class NagTest {

    @ParameterizedTest
    @EnumSource(
        value = Nag.class,
        mode = EnumSource.Mode.EXCLUDE,
        names = {"GOOD_MOVE", "BRILLANT_MOVE"})
    void mustBeTrained_bad(final Nag nag) {

        assertThat(nag.mustBeTrained()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(
        value = Nag.class,
        mode = EnumSource.Mode.INCLUDE,
        names = {"GOOD_MOVE", "BRILLANT_MOVE"})
    void mustBeTrained_good(final Nag nag) {

        assertThat(nag.mustBeTrained()).isTrue();
    }

}
