package jp.ac.keio.ics.db.utils;

import org.apache.commons.lang3.StringUtils;


public class StringSimilarity {

  /**
   * Calculates the similarity (a number within 0 and 1) between two strings.
   */
  public static double similarity(String s1, String s2) {
    String longer = s1, shorter = s2;
    if (s1.length() < s2.length()) { // longer should always have greater length
      longer = s2; shorter = s1;
    }
    int longerLength = longer.length();
    if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
    /* // If you have StringUtils, you can use it to calculate the edit distance:
    return (longerLength - StringUtils.getLevenshteinDistance(longer, shorter)) /
                               (double) longerLength; */
    return (longerLength - StringUtils.getLevenshteinDistance(longer, shorter)) / (double) longerLength;

  }

  public static void printSimilarity(String s, String t) {
    System.out.println(String.format(
      "%.3f is the similarity between \"%s\" and \"%s\"", similarity(s, t), s, t));
  }

}
