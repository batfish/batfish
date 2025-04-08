package org.batfish.grammar.flatjuniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.representation.juniper.CommunityMemberParseResult;
import org.batfish.representation.juniper.LiteralCommunityMember;
import org.batfish.representation.juniper.RegexCommunityMember;
import org.junit.Test;

public class CommunityMemberTest {

  @Test
  public void testParseEmptyColon() {
    CommunityMemberParseResult result = ConfigurationBuilder.parseCommunityMember(":");
    assertThat(result.getMember(), instanceOf(LiteralCommunityMember.class));
    LiteralCommunityMember member = (LiteralCommunityMember) result.getMember();
    assertThat(member.getCommunity(), equalTo(StandardCommunity.of(0, 0)));
    assertThat(result.getWarning(), equalTo("RISKY: Community string ':' is interpreted as '0:0'"));
  }

  @Test
  public void testParseColonWithAsn() {
    CommunityMemberParseResult result = ConfigurationBuilder.parseCommunityMember("123:");
    assertThat(result.getMember(), instanceOf(LiteralCommunityMember.class));
    LiteralCommunityMember member = (LiteralCommunityMember) result.getMember();
    assertThat(member.getCommunity(), equalTo(StandardCommunity.of(123, 0)));
    assertThat(
        result.getWarning(), equalTo("RISKY: Community string '123:' is interpreted as '123:0'"));
  }

  @Test
  public void testParseColonWithValue() {
    CommunityMemberParseResult result = ConfigurationBuilder.parseCommunityMember(":123");
    assertThat(result, notNullValue());
    assertThat(result.getMember(), instanceOf(LiteralCommunityMember.class));
    LiteralCommunityMember member = (LiteralCommunityMember) result.getMember();
    assertThat(member.getCommunity(), equalTo(StandardCommunity.of(0, 123)));
    assertThat(result.getWarning(), notNullValue());
    assertThat(
        result.getWarning(), equalTo("RISKY: Community string ':123' is interpreted as '0:123'"));
  }

  @Test
  public void testParseDigitOnly() {
    CommunityMemberParseResult result = ConfigurationBuilder.parseCommunityMember("123");
    assertThat(result, notNullValue());
    assertThat(result.getMember(), instanceOf(RegexCommunityMember.class));
    RegexCommunityMember member = (RegexCommunityMember) result.getMember();
    assertThat(member.getRegex(), equalTo(".*123.*"));
    assertThat(result.getWarning(), nullValue());
  }

  @Test
  public void testParseStandardCommunity() {
    CommunityMemberParseResult result = ConfigurationBuilder.parseCommunityMember("123:456");
    assertThat(result, notNullValue());
    assertThat(result.getMember(), instanceOf(LiteralCommunityMember.class));
    LiteralCommunityMember member = (LiteralCommunityMember) result.getMember();
    assertThat(member.getCommunity(), equalTo(StandardCommunity.of(123, 456)));
    assertThat(result.getWarning(), nullValue());
  }

  @Test
  public void testParseRegexWithUnintendedMatches() {
    CommunityMemberParseResult result = ConfigurationBuilder.parseCommunityMember("123:.*");
    assertThat(result, notNullValue());
    assertThat(result.getMember(), instanceOf(RegexCommunityMember.class));
    RegexCommunityMember member = (RegexCommunityMember) result.getMember();
    assertThat(member.getRegex(), equalTo("123:.*"));
    assertThat(result.getWarning(), notNullValue());
  }
}
