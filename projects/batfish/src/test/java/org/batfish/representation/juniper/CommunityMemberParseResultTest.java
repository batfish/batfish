package org.batfish.representation.juniper;

import static org.batfish.representation.juniper.CommunityMemberParseResult.parseCommunityMember;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Test for {@link CommunityMemberParseResult} */
public class CommunityMemberParseResultTest {

  @Test
  public void testParseCommunityMember() {
    // Literal cases
    assertThat(
        parseCommunityMember("123:456"),
        equalTo(
            new CommunityMemberParseResult(
                new LiteralCommunityMember(StandardCommunity.of(123, 456)), null)));
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
    assertThat(
        parseCommunityMember("origin:111:222"),
        equalTo(
            new CommunityMemberParseResult(
                new LiteralCommunityMember(ExtendedCommunity.of(0x03, 111, 222)), null)));
    assertThat(
        parseCommunityMember("origin:111:"),
        equalTo(
            new CommunityMemberParseResult(
                new LiteralCommunityMember(ExtendedCommunity.of(0x03, 111, 0)),
                "RISK: Community string 'origin:111:' is interpreted as 'origin:111:0'")));
    assertThat(
        parseCommunityMember("large:111:222:333"),
        equalTo(
            new CommunityMemberParseResult(
                new LiteralCommunityMember(LargeCommunity.of(111, 222, 333)), null)));
    assertThat(
        parseCommunityMember("large:111:222:"),
        equalTo(
            new CommunityMemberParseResult(
                new LiteralCommunityMember(LargeCommunity.of(111, 222, 0)),
                "RISK: Community string 'large:111:222:' is interpreted as 'large:111:222:0'")));
    assertThat(
        parseCommunityMember("large:111::"),
        equalTo(
            new CommunityMemberParseResult(
                new LiteralCommunityMember(LargeCommunity.of(111, 0, 0)),
                "RISK: Community string 'large:111::' is interpreted as 'large:111:0:0'")));
    assertThat(
        parseCommunityMember("large:::"),
        equalTo(
            new CommunityMemberParseResult(
                new LiteralCommunityMember(LargeCommunity.of(0, 0, 0)),
                "RISK: Community string 'large:::' is interpreted as 'large:0:0:0'")));
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
