package org.batfish.representation.juniper;

import static org.batfish.representation.juniper.CommunityMemberParseResult.parseCommunityMember;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Test for {@link CommunityMemberParseResult} */
public class CommunityMemberParseResultTest {

  @Test
  public void testParseCommunityMember() {
    // Literal cases
    assertThat(
        parseCommunityMember(":"),
        equalTo(
            new CommunityMemberParseResult(
                new LiteralCommunityMember(StandardCommunity.of(0, 0)),
                "RISK: Community string ':' is interpreted as '0:0'")));
    assertThat(
        parseCommunityMember(":123"),
        equalTo(
            new CommunityMemberParseResult(
                new LiteralCommunityMember(StandardCommunity.of(0, 123)),
                "RISK: Community string ':123' is interpreted as '0:123'")));
    assertThat(
        parseCommunityMember("123:456"),
        equalTo(
            new CommunityMemberParseResult(
                new LiteralCommunityMember(StandardCommunity.of(123, 456)), null)));
    // Regex cases
    assertThat(
        parseCommunityMember("123"),
        equalTo(new CommunityMemberParseResult(new RegexCommunityMember("123"), null)));
    assertThat(
        parseCommunityMember("123:.*"),
        equalTo(
            new CommunityMemberParseResult(
                new RegexCommunityMember("123:.*"),
                "RISK: Community regex 123:.* allows longer matches such as 1123:0")));
    assertThat(
        parseCommunityMember(".*:.*"),
        equalTo(new CommunityMemberParseResult(new RegexCommunityMember(".*:.*"), null)));
    assertThat(
        parseCommunityMember("^123:456$"),
        equalTo(new CommunityMemberParseResult(new RegexCommunityMember("^123:456$"), null)));
  }
}
