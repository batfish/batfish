package org.batfish.datamodel.routing_policy.communities;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import javax.annotation.Nonnull;
import org.apache.commons.text.StringEscapeUtils;
import org.batfish.datamodel.routing_policy.Common;

/** Utility class for constructing {@link CommunitySetExpr}s. */
public final class CommunitySetExprs {

  public static @Nonnull CommunitySetExpr empty() {
    return EMPTY;
  }

  private static final CommunitySetExpr EMPTY = new LiteralCommunitySet(CommunitySet.empty());

  private CommunitySetExprs() {}

  /**
   * Creates an expression that matches on the given community-set regex. If the regex can be
   * identified as one that only requires the existence of a single community, then the result makes
   * that explicit by using {@link CommunityMatchRegex} instead of {@link CommunitySetMatchRegex}.
   *
   * @param regex the regex
   * @param communityRendering the String rendering to use for each community
   * @return an expression that represents a community-set match on the regex
   */
  public static @Nonnull CommunitySetMatchExpr toMatchExpr(
      String regex, CommunityRendering communityRendering) {
    String trimmedRegex = regex;
    // a conservative check to determine if the regex only matches on the existence of a single
    // community in the set: the regex optionally starts with _, optionally ends with _, and in
    // between only accepts strings containing digits and colons
    String underscore = StringEscapeUtils.unescapeJava(Common.DEFAULT_UNDERSCORE_REPLACEMENT);
    if (trimmedRegex.startsWith(underscore)) {
      trimmedRegex = trimmedRegex.substring(underscore.length());
    }
    if (trimmedRegex.endsWith(underscore)) {
      trimmedRegex = trimmedRegex.substring(0, trimmedRegex.length() - underscore.length());
    }
    Automaton trimmedRegexAuto = new RegExp(trimmedRegex).toAutomaton();
    Automaton digitsAndColons = new RegExp("[0-9:]+").toAutomaton();
    if (trimmedRegexAuto.intersection(digitsAndColons).equals(trimmedRegexAuto)) {
      return new HasCommunity(new CommunityMatchRegex(communityRendering, regex));
    } else {
      return new CommunitySetMatchRegex(
          new TypesFirstAscendingSpaceSeparated(communityRendering), regex);
    }
  }

  /**
   * Returns {@link #toMatchExpr(String, CommunityRendering)} using {@link
   * ColonSeparatedRendering#instance()} as the {@link CommunityRendering}.
   *
   * <p>New clients of this class should use {@link #toMatchExpr(String, CommunityRendering)}
   * directly.
   */
  public static @Nonnull CommunitySetMatchExpr toMatchExpr(String regex) {
    return toMatchExpr(regex, ColonSeparatedRendering.instance());
  }
}
