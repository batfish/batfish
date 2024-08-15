package org.batfish.representation.juniper;

import static org.batfish.representation.juniper.RegexCommunityMember.isRiskyCommunityRegex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.junit.Test;

public class RegexCommunityMemberTest {

  @Test
  public void testIsRiskyCommunityRegex() {
    for (String notRisky :
        new String[] {
          "12345:6..$", "12345:[67]....", "^1:2$", ".*:.*", "[0-9]*:[0-9]*", "12345:4{5}"
        }) {
      String maybeRisky = isRiskyCommunityRegex(notRisky).orElse("not risky");
      assertThat(maybeRisky, equalTo("not risky"));
    }
    for (String risky : new String[] {"123:4", "^123:4", "123:4$", "12345:5[12][34]"}) {
      String maybeRisky = isRiskyCommunityRegex(risky).orElse("not risky");
      assertThat(maybeRisky, not(equalTo("not risky")));
    }
    // Test a few warnings of interest
    assertThat(isRiskyCommunityRegex("123:4").get(), equalTo("1123:4 and 123:40"));
    assertThat(isRiskyCommunityRegex("123:4$").get(), equalTo("1123:4"));
    assertThat(isRiskyCommunityRegex("^123:4").get(), equalTo("123:40"));
  }
}
