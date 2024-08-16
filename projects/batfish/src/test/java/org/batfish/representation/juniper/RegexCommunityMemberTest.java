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
}
