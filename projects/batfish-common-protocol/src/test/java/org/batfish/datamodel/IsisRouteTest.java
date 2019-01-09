package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.isis.IsisLevel;
import org.junit.Test;

/** Tests of {@link IsisRoute} */
public class IsisRouteTest {
  @Test
  public void testEqualsAndToBuilder() {
    IsisRoute.Builder b =
        new IsisRoute.Builder()
            .setNetwork(Prefix.parse("1.1.1.1/32"))
            .setLevel(IsisLevel.LEVEL_1)
            .setArea("0")
            .setNextHopIp(Ip.parse("2.2.2.2"))
            .setProtocol(RoutingProtocol.ISIS_L1)
            .setSystemId("id");

    IsisRoute original = b.build();
    IsisRoute copy = original.toBuilder().build();

    assertThat(copy, equalTo(original));
  }
}
