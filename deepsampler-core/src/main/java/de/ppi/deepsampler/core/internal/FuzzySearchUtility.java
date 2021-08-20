package de.ppi.deepsampler.core.internal;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FuzzySearchUtility {


    /**
     * Searches for wantedString in candidates. The search tries to find the String that has the most similarity,
     * perfect equality is not necessary.
     *
     * @param wantedString The String that is searched in candidates
     * @param candidates   A {@link List} of Strings that might be equal, or similar to wantedString.
     * @return A pair containing the best matching candidate and a percentage value that shows the similarity.
     */
    public static <T> Match<String> findClosestString(String wantedString, List<String> candidates) {
        return findClosestString(wantedString, candidates, String::toString);
    }

    /**
     * Searches for wantedString in candidates. candidateStringProvider() is used to get the String from each candidate, that is used for comparison.
     * The search tries to find the String that has the most similarity, perfect equality is not necessary.
     *
     * @param wantedString The String that is searched in candidates
     * @param candidates   A {@link List} of Objects that might have a String that is equal, or similar to wantedString.
     * @param candidateStringProvider A functional interface, that should provide the String from a candidate, that is used for the comparison.
     * @return A pair containing the best matching candidate and a percentage value that shows the similarity.
     */
    public static <T> Match<T> findClosestString(String wantedString, List<T> candidates, Function<T, String> candidateStringProvider) {
        if (candidates.size() == 0) {
            return null;
        }

        List<Match<T>> matchedCandidates = candidates.stream()
                .map(candidate -> new Match<>(candidate, calcEquality(candidateStringProvider.apply(candidate), wantedString)))
                .sorted(Comparator.comparingDouble(Match<T>::getEquality))
                .collect(Collectors.toList());

        return matchedCandidates.get(matchedCandidates.size() - 1);
    }


    /**
     * Calculates how different the two Strings left and right are.
     *
     * @param left  One of the two Strings that are compared.
     * @param right The other of two Strings that are compared.
     * @return A value between 0 and 1 where 0 means the Strings are completely different and 1 means, that both Strings are equal.
     * @see <a href="https://stackoverflow.com/questions/955110/similarity-string-comparison-in-java"/>
     */
    public static double calcEquality(String left, String right) {
        String longer = left;
        String shorter = right;

        if (left.length() < right.length()) { // longer should always have greater length
            longer = right;
            shorter = left;
        }

        int longerLength = longer.length();

        if (longerLength == 0) {
            return 1.0; // both strings are zero length
        }

        return (longerLength - calcEditDistance(longer, shorter)) / (double) longerLength;
    }


    /**
     * Calculates the difference between two Strings using the "Levenshtein Edit Distance" algorithm.
     *
     * @param left  One of the two Strings that are compared.
     * @param right The other of two Strings that are compared.
     * @return the cost of converting left into right. This can be used to measure the difference between left and right.
     * @see <a href="https://stackoverflow.com/questions/955110/similarity-string-comparison-in-java"/>
     */
    private static int calcEditDistance(String left, String right) {
        left = left.toLowerCase();
        right = right.toLowerCase();

        int[] costs = new int[right.length() + 1];
        for (int i = 0; i <= left.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= right.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];

                        if (left.charAt(i - 1) != right.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }

                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }

            if (i > 0) {
                costs[right.length()] = lastValue;
            }
        }

        return costs[right.length()];
    }

    /**
     * Describes a String that matches to another String. How similar the compared Strings are is expressed by
     * {@link Match#getEquality()}.
     */
    public static class Match<T> {
        private final double equality;
        private final T matchedObject;

        public Match(T matchedObject, double equality) {
            this.equality = equality;
            this.matchedObject = matchedObject;
        }

        /**
         * The extend of equality. A 0 means no equality at all and a 1 means perfect equality.
         *
         * @return The extend of equality.
         */
        public double getEquality() {
            return equality;
        }

        public T getMatchedObject() {
            return matchedObject;
        }
    }
}
