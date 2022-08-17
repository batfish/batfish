package org.batfish.representation.juniper;

/**
 * A class that converts a Juniper AS Path regex to a Java regex.
 *
 * @see <a
 *     href="https://www.juniper.net/documentation/en_US/junos/topics/usage-guidelines/policy-configuring-as-path-regular-expressions-to-use-as-routing-policy-match-conditions.html">Juniper
 *     docs</a>
 */
public final class AsPathRegex {
  /** Converts the given Juniper AS Path regular expression to a Java regular expression. */
  public static String convertToJavaRegex(String regex) {
    return org.batfish.representation.juniper.parboiled.AsPathRegex.convertToJavaRegex(regex);
  }

  private AsPathRegex() {} // prevent instantiation of utility class.
}
