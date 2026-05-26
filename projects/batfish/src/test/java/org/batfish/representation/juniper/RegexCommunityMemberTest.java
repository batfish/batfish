package org.batfish.representation.juniper;

import static org.batfish.representation.juniper.RegexCommunityMember.getUnintendedCommunityMatches;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import org.junit.Test;

public class RegexCommunityMemberTest {

  @Test
  public void testIsRiskyCommunityRegex() {
    assertThat(getUnintendedCommunityMatches("^1:2$"), empty());
    assertThat(getUnintendedCommunityMatches("12345:23456"), empty());
    assertThat(getUnintendedCommunityMatches(".....:....."), empty());
    assertThat(getUnintendedCommunityMatches(".*:.*"), empty());
    assertThat(getUnintendedCommunityMatches("*:*"), empty());
    assertThat(getUnintendedCommunityMatches("12345:*"), empty());
    assertThat(getUnintendedCommunityMatches("*:12345"), empty());
    assertThat(getUnintendedCommunityMatches("[0-9]*:[0-9]*"), empty());
    assertThat(getUnintendedCommunityMatches("[0-9]+:[0-9]+"), empty());
    assertThat(getUnintendedCommunityMatches("[0-9]{5}:[0-9]{5}"), empty());
    assertThat(getUnintendedCommunityMatches("12345:6..$"), empty());
    assertThat(getUnintendedCommunityMatches("12345:4{5}"), empty());
    assertThat(getUnintendedCommunityMatches("12345:[67]...."), empty());

    assertThat(getUnintendedCommunityMatches("123:4"), contains("1123:4", "123:40"));
    assertThat(getUnintendedCommunityMatches("^123:4"), contains("123:40"));
    assertThat(getUnintendedCommunityMatches("123:4$"), contains("1123:4"));
    assertThat(getUnintendedCommunityMatches("12345:5[12][34]"), contains("12345:5130"));
    assertThat(getUnintendedCommunityMatches("12345:"), contains("12345:0"));
  }

  /**
   * The dk.brics RegExp syntax reserves chars (e.g., {@code <>~&#@"}) that have no special meaning
   * in Java regex. Such chars must be escaped before passing to dk.brics so analysis doesn't crash.
   * The chars themselves can never appear in a real BGP community (digits and {@code :} only), so
   * any regex containing them as literals matches nothing valid; analysis still proceeds on the
   * surrounding parts of the regex.
   *
   * <p>Verified on vJunos 25.4R1.12 that {@code set policy-options community FOO members
   * "12345<?:1"} (quoted or unquoted) is accepted by the CLI and passes {@code commit check}, so
   * Batfish must handle it.
   */
  @Test
  public void testDkBricsReservedCharsDoNotCrash() {
    // 12345<?:1 is literal "12345", optional "<", then ":1" — analysis suggests "12345:10" which
    // is reasonable: the regex matches both "12345:1" and "12345<:1" and can extend at the end.
    assertThat(getUnintendedCommunityMatches("12345<?:1"), contains("12345:10"));
    // Each of these chars is literal in Java regex but special in dk.brics; escaping prevents the
    // crash. Returns empty because the literal char rules out matching any standard community.
    assertThat(getUnintendedCommunityMatches("123<:456"), empty());
    assertThat(getUnintendedCommunityMatches("123~:456"), empty());
    assertThat(getUnintendedCommunityMatches("123&:456"), empty());
    assertThat(getUnintendedCommunityMatches("123@:456"), empty());
    assertThat(getUnintendedCommunityMatches("123#:456"), empty());
    assertThat(getUnintendedCommunityMatches("123\":456"), empty());
  }
}
