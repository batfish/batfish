package org.batfish.representation.juniper;

/** Utility to convert a JunOS group wildcard into a Java regex. */
public final class GroupWildcard {
  public static String toJavaRegex(String wildcard) {
    return org.batfish.representation.juniper.parboiled.GroupWildcard.toJavaRegex(wildcard);
  }

  private GroupWildcard() {} // prevent instantiation of utility class.
}
