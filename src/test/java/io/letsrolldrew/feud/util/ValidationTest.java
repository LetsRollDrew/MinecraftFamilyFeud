package io.letsrolldrew.feud.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ValidationTest {

    @Test
    void requireNonBlankReturnsTrimmed() {
        String result = Validation.requireNonBlank("  value  ", "field");
        assertEquals("value", result);
    }

    @Test
    void requireNonBlankThrowsOnBlank() {
        assertThrows(IllegalArgumentException.class, () -> Validation.requireNonBlank("   ", "field"));
        assertThrows(IllegalArgumentException.class, () -> Validation.requireNonBlank(null, "field"));
    }

    @Test
    void requirePositiveAcceptsPositive() {
        assertEquals(5, Validation.requirePositive(5, "num"));
    }

    @Test
    void requirePositiveThrowsOnZeroOrNegative() {
        assertThrows(IllegalArgumentException.class, () -> Validation.requirePositive(0, "num"));
        assertThrows(IllegalArgumentException.class, () -> Validation.requirePositive(-2, "num"));
    }

    @Test
    void requireInRangeAcceptsBounds() {
        assertEquals(1, Validation.requireInRange(1, 1, 5, "num"));
        assertEquals(5, Validation.requireInRange(5, 1, 5, "num"));
    }

    @Test
    void requireInRangeThrowsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> Validation.requireInRange(0, 1, 5, "num"));
        assertThrows(IllegalArgumentException.class, () -> Validation.requireInRange(6, 1, 5, "num"));
    }
}
