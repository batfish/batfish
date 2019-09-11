package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Test of {@link CommunityToRegexInputString}. */
public final class CommunityToRegexInputStringTest {

  @Test
  public void testVisitColonSeparatedRendering() {
    assertThat(
        ColonSeparatedRendering.instance()
            .accept(CommunityToRegexInputString.instance(), StandardCommunity.of(1, 1)),
        equalTo("1:1"));

    // TODO: implement and change expected value
    assertThat(
        ColonSeparatedRendering.instance()
            .accept(CommunityToRegexInputString.instance(), ExtendedCommunity.of(0, 0L, 0L)),
        equalTo(""));

    assertThat(
        ColonSeparatedRendering.instance()
            .accept(CommunityToRegexInputString.instance(), LargeCommunity.of(0L, 0L, 0L)),
        equalTo("0:0:0"));
  }

  @Test
  public void testVisitIntegerValueRendering() {
    assertThat(
        IntegerValueRendering.instance()
            .accept(CommunityToRegexInputString.instance(), StandardCommunity.of(1, 1)),
        equalTo("65537"));
    assertThat(
        IntegerValueRendering.instance()
            .accept(CommunityToRegexInputString.instance(), ExtendedCommunity.of(0, 0L, 0L)),
        equalTo("0"));
    assertThat(
        IntegerValueRendering.instance()
            .accept(CommunityToRegexInputString.instance(), LargeCommunity.of(0L, 0L, 0L)),
        equalTo("0"));
  }
}
