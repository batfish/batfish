package org.batfish.datamodel;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

/** Test for {@link OspfExternalType2Route} */
public class OspfExternalType2RouteTest {

  @Test
  public void testEquals() {

    OspfExternalType2Route r1 =
        new OspfExternalType2Route(new Prefix(new Ip("1.1.1.1"), 32), Ip.ZERO, 1, 1, 1, 1, 1, "");
    OspfExternalType2Route r1DiffObj =
        new OspfExternalType2Route(new Prefix(new Ip("1.1.1.1"), 32), Ip.ZERO, 1, 1, 1, 1, 1, "");
    OspfExternalType2Route r2 =
        new OspfExternalType2Route(new Prefix(new Ip("1.1.1.1"), 32), Ip.ZERO, 1, 1, 1, 1, 2, "");
    OspfExternalType1Route type1Route =
        new OspfExternalType1Route(new Prefix(new Ip("1.1.1.1"), 32), Ip.ZERO, 1, 1, 1, 1, 2, "");

    // Simple equality checks
    assertThat(r1, equalTo(r1));
    assertThat(r1, equalTo(r1DiffObj));
    assertThat(r1, not(equalTo(nullValue())));

    // Cost to advertiser differs
    assertThat(r1, not(equalTo(r2)));
    // Not the same type
    assertThat(r1, not(equalTo(type1Route)));
  }
}
