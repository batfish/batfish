package org.batfish.datamodel.routing_policy.communities;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.junit.Test;

/** Test of {@link CommunityRenderer}. */
public final class CommunityRendererTest {

  @Test
  public void testVisitColonSeparatedRendering() {
    assertThat(
        ColonSeparatedRendering.instance()
            .accept(new CommunityRenderer(StandardCommunity.of(1, 1))),
        equalTo("1:1"));

    // TODO: implement and change expected value
    assertThat(
        ColonSeparatedRendering.instance()
            .accept(new CommunityRenderer(ExtendedCommunity.of(0, 0L, 0L))),
        equalTo(""));

    assertThat(
        ColonSeparatedRendering.instance()
            .accept(new CommunityRenderer(LargeCommunity.of(0L, 0L, 0L))),
        equalTo("0:0:0"));
  }

  @Test
  public void testVisitIntegerValueRendering() {
    assertThat(
        IntegerValueRendering.instance().accept(new CommunityRenderer(StandardCommunity.of(1, 1))),
        equalTo("65537"));
    assertThat(
        IntegerValueRendering.instance()
            .accept(new CommunityRenderer(ExtendedCommunity.of(0, 0L, 0L))),
        equalTo("0"));
    assertThat(
        IntegerValueRendering.instance()
            .accept(new CommunityRenderer(LargeCommunity.of(0L, 0L, 0L))),
        equalTo("0"));
  }
}
