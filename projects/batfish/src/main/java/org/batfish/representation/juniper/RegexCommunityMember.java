package org.batfish.representation.juniper;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.Community;

/** A {@link CommunityMember} representing a regex match condition for a {@link Community}. */
@ParametersAreNonnullByDefault
public final class RegexCommunityMember implements CommunityMember {
  private static String communityRegexToJavaRegex(String regex) {
    String out = regex;
    out = out.replace(":*", ":.*");
    out = out.replaceFirst("^\\*", ".*");
    return out;
  }

  public static Optional<String> isRiskyCommunityRegex(String junosRegex) {
    String javaRegex = communityRegexToJavaRegex(junosRegex);
    String u16 = "((0|[1-9][0-9]*)&<0-65535>)";
    String regex = javaRegex.replaceAll("\\.", "[0-9]");

    Automaton valid = new RegExp("^^" + u16 + ":" + u16 + "$$").toAutomaton();
    RegExp r = new RegExp(".*" + regex + ".*");
    RegExp rClipped = new RegExp(".*^" + regex + "$.*");
    Automaton validR = r.toAutomaton().intersection(valid);
    Automaton validClipped = rClipped.toAutomaton().intersection(valid);
    if (validR.equals(validClipped)) {
      return Optional.empty();
    }
    Automaton exampleExtendingFront =
        new RegExp(".*" + regex + "$.*").toAutomaton().intersection(valid).minus(validClipped);
    Automaton exampleExtendingEnd =
        new RegExp(".*^" + regex + ".*").toAutomaton().intersection(valid).minus(validClipped);
    List<String> examples = new LinkedList<>();
    if (!exampleExtendingFront.minus(validClipped).isEmpty()) {
      String example = exampleExtendingFront.getShortestExample(true);
      assert example.startsWith("^^") && example.endsWith("$$");
      examples.add(example.substring(2, example.length() - 2));
    }
    if (!exampleExtendingEnd.minus(validClipped).isEmpty()) {
      String example = exampleExtendingEnd.getShortestExample(true);
      assert example.startsWith("^^") && example.endsWith("$$");
      examples.add(example.substring(2, example.length() - 2));
    }
    return Optional.of(String.join(" and ", examples));
  }

  public RegexCommunityMember(String regex) {
    _regex = regex;
  }

  @Override
  public <T> T accept(CommunityMemberVisitor<T> visitor) {
    return visitor.visitRegexCommunityMember(this);
  }

  /**
   * The raw text of the regex in Junos syntax.
   *
   * @see #getJavaRegex()
   */
  public @Nonnull String getRegex() {
    return _regex;
  }

  /**
   * A representation of this regex that can be used in Java to match against community strings.
   *
   * @see #getRegex()
   */
  public @Nonnull String getJavaRegex() {
    return communityRegexToJavaRegex(_regex);
  }

  private final @Nonnull String _regex;
}
