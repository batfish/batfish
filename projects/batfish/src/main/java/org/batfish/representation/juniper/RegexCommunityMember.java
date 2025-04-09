package org.batfish.representation.juniper;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.Community;

/** A {@link CommunityMember} representing a regex match condition for a {@link Community}. */
@ParametersAreNonnullByDefault
public final class RegexCommunityMember implements CommunityMember {
  private static final LoadingCache<String, List<String>> REGEX_CACHE =
      Caffeine.newBuilder()
          .maximumSize(2048)
          .build(RegexCommunityMember::getUnintendedCommunityMatchesImpl);

  /**
   * In Junos regexes, you can use {@code *} as a shortcut for {@code .*}, if it's alone. That is,
   * {@code *:5$} is the same as {@code .*:5$} or {@code [0-9]*:5$}.
   *
   * <p>Handle this by converting standalone {@code *} literals to {@code .*} for use in Java
   * regexes.
   *
   * <p>TODO: this likely needs more work for regexes over extended communities or malformed
   * regexes.
   *
   * <p>See <a
   * href="https://www.juniper.net/documentation/us/en/software/junos/routing-policy/topics/concept/policy-bgp-communities-extended-communities-evaluation-in-routing-policy-match-conditions.html">Junos
   * documentation</a>}
   */
  private static String communityRegexToJavaRegex(String regex) {
    String out = regex;
    out = out.replace(":*", ":.*");
    out = out.replaceFirst("^\\*", ".*");
    return out;
  }

  /**
   * Returns a list of {@link Community} literals that may be unintended matches for the given
   * regex. The definition of "unintended" is if it matches the given regex, but does not match if
   * the regex is limited with {@code ^} and {@code $}.
   *
   * <p>Note that in modern Junos releases, you do not need to truncate "longest possible" matches
   * like {@code 11111:*} because there is no standard-community that starts with 6 digits. So we
   * must constrain to regex-type-specific regexes.
   *
   * <p>TODO: this function currently handles only standard community regexes.
   *
   * <p>See <a
   * href="https://www.juniper.net/documentation/us/en/software/junos/routing-policy/topics/concept/policy-bgp-communities-extended-communities-evaluation-in-routing-policy-match-conditions.html">Junos
   * documentation</a>}
   */
  public static List<String> getUnintendedCommunityMatches(String junosRegex) {
    return REGEX_CACHE.get(junosRegex);
  }

  private static List<String> getUnintendedCommunityMatchesImpl(String junosRegex) {
    if (junosRegex.split(":", -1).length != 2) {
      return ImmutableList.of();
    }
    String javaRegex = communityRegexToJavaRegex(junosRegex);

    // In Junos, a valid standard community regex is u16:u16.
    //
    // Note: this assumes a user doesn't do something like [community FOO members "no-ex.ort"]
    // to turn a well-known community into a regex. It may not work anyway, but we haven't tested
    // it. There's no good reason for this to happen, as community matches can mix regexes and
    // literals, unlike many Cisco-style platforms.
    String u16 = "((0|[1-9][0-9]*)&<0-65535>)";
    // The user's regex may already have ^$, but we will unconditionally append them. Therefore, we
    // must permit ^^ and $$. The automaton library does not implement ^ and $ as start/stop.
    Automaton valid = new RegExp("^^" + u16 + ":" + u16 + "$$").toAutomaton();

    // This replacement is necessary because otherwise something like .....:..... will be treated as
    // allowing shorter communities such as 1:2 by matching, e.g., xx^^1:2$$xx. Make the . only
    // match numbers to disallow this. This stems from the lack of ^$ support.
    String regex = javaRegex.replaceAll("\\.", "[0-9]");

    RegExp r = new RegExp(".*" + regex + ".*");
    RegExp rClipped = new RegExp("^?^" + regex + "$$?");
    Automaton validR = r.toAutomaton().intersection(valid);
    Automaton validClipped = rClipped.toAutomaton().intersection(valid);
    if (validR.equals(validClipped)) {
      return ImmutableList.of();
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
    return examples;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RegexCommunityMember)) {
      return false;
    }
    RegexCommunityMember rcm = (RegexCommunityMember) o;
    return Objects.equals(_regex, rcm._regex);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_regex);
  }
}
