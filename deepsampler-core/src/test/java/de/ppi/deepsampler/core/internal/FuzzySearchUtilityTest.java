package de.ppi.deepsampler.core.internal;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FuzzySearchUtilityTest {

    @Test
    void findClosestString() {
        // GIVEN
        List<String> candidates = Arrays.asList("X", "ABAB", "C", "D");
        List<String> emptyCandidates = new ArrayList<>();

        // WHEN
        FuzzySearchUtility.Match match = FuzzySearchUtility.findClosestString("AB", candidates);
        FuzzySearchUtility.Match emptyMatch = FuzzySearchUtility.findClosestString("AB", emptyCandidates);

        // THEN
        assertNotNull(match);
        assertEquals("ABAB", match.getMatchedString());
        assertNull(emptyMatch);
    }

    @Test
    void similarity() {
        assertEquals(1.0, FuzzySearchUtility.calcEquality("A", "A"));
        assertEquals(0.0, FuzzySearchUtility.calcEquality("A", "B"));
        assertEquals(0.5, FuzzySearchUtility.calcEquality("AB", "B"));
        assertEquals(0.5, FuzzySearchUtility.calcEquality("BA", "B"));
        assertEquals(0.25, FuzzySearchUtility.calcEquality("ABAC", "B"));
        assertEquals(0.5, FuzzySearchUtility.calcEquality("ABAB", "CBCB"));
    }
}