package com.github.ngeor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SemVerTest {
    @Nested
    class ConstructorTest {
        @Test
        void valid() {
            SemVer semVer = new SemVer(1, 2, 3, "suffix");
            assertThat(semVer.major()).isEqualTo(1);
            assertThat(semVer.minor()).isEqualTo(2);
            assertThat(semVer.patch()).isEqualTo(3);
            assertThat(semVer.suffix()).isEqualTo("suffix");
        }

        @Test
        void validNullSuffix() {
            SemVer semVer = new SemVer(1, 2, 3, null);
            assertThat(semVer.major()).isEqualTo(1);
            assertThat(semVer.minor()).isEqualTo(2);
            assertThat(semVer.patch()).isEqualTo(3);
            assertThat(semVer.suffix()).isNull();
        }

        @Test
        void negativeMajor() {
            assertThatThrownBy(() -> new SemVer(-1, 0, 0, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Major version cannot be negative");
        }

        @Test
        void negativeMinor() {
            assertThatThrownBy(() -> new SemVer(0, -1, 0, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Minor version cannot be negative");
        }

        @Test
        void negativePatch() {
            assertThatThrownBy(() -> new SemVer(0, 0, -1, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Patch version cannot be negative");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        void blankSuffix(String suffix) {
            assertThatThrownBy(() -> new SemVer(0, 0, 0, suffix))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Version suffix cannot be blank");
        }
    }

    @Nested
    class ToStringTest {
        @Test
        void withSuffix() {
            SemVer semVer = new SemVer(1, 2, 3, "beta");
            assertThat(semVer.toString()).isEqualTo("1.2.3-beta");
        }

        @Test
        void withoutSuffix() {
            SemVer semVer = new SemVer(1, 2, 3, null);
            assertThat(semVer.toString()).isEqualTo("1.2.3");
        }
    }

    @Nested
    class ParseTest {
        @Test
        void valid() {
            SemVer semVer = SemVer.parse("1.2.3-beta");
            assertThat(semVer).isEqualTo(new SemVer(1, 2, 3, "beta"));
        }

        @Test
        void validNoSuffix() {
            SemVer semVer = SemVer.parse("1.2.3");
            assertThat(semVer).isEqualTo(new SemVer(1, 2, 3, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " "})
        void invalid(String version) {
            assertThatThrownBy(() -> SemVer.parse(version))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void invalidFormat() {
            assertThatThrownBy(() -> SemVer.parse("1.2"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void invalidNumber() {
            assertThatThrownBy(() -> SemVer.parse("a.b.c"))
                    .isInstanceOf(NumberFormatException.class);
        }
    }

    @Nested
    class CompareToTest {
        @Test
        void equal() {
            SemVer v1 = new SemVer(1, 2, 3, "suffix");
            SemVer v2 = new SemVer(1, 2, 3, "suffix");
            assertThat(v1).isEqualByComparingTo(v2);
        }

        @Test
        void majorDifference() {
            SemVer v1 = new SemVer(1, 2, 3, null);
            SemVer v2 = new SemVer(2, 2, 3, null);
            assertThat(v1).isLessThan(v2);
        }

        @Test
        void minorDifference() {
            SemVer v1 = new SemVer(1, 2, 3, null);
            SemVer v2 = new SemVer(1, 3, 3, null);
            assertThat(v1).isLessThan(v2);
        }

        @Test
        void patchDifference() {
            SemVer v1 = new SemVer(1, 2, 3, null);
            SemVer v2 = new SemVer(1, 2, 4, null);
            assertThat(v1).isLessThan(v2);
        }

        @Test
        void suffixDifference() {
            SemVer v1 = new SemVer(1, 2, 3, "alpha");
            SemVer v2 = new SemVer(1, 2, 3, "beta");
            assertThat(v1).isLessThan(v2);
        }

        @Test
        void nullSuffixIsGreater() {
            SemVer v1 = new SemVer(1, 2, 3, "beta");
            SemVer v2 = new SemVer(1, 2, 3, null);
            assertThat(v1).isLessThan(v2);
        }

        @Test
        void bothNullSuffix() {
            SemVer v1 = new SemVer(1, 2, 3, null);
            SemVer v2 = new SemVer(1, 2, 3, null);
            assertThat(v1).isEqualByComparingTo(v2);
        }
    }
}
