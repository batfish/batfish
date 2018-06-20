package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

/** Tests of {@link AbstractRoute} */
public class AbstractRouteTest {

  @Test
  public void testDefaultFullString() {
    AbstractRoute route = StaticRoute.builder().setNetwork(Prefix.parse("1.1.1.0/24")).build();

    String expected =
        "null vrf:null net:1.1.1.0/24 nhip:AUTO/NONE(-1l) nhint:dynamic nhnode:null admin:-1 cost:0 tag:none prot:STATIC  tag:-1";
    assertThat(route.fullString(), equalTo(expected));
  }
}
