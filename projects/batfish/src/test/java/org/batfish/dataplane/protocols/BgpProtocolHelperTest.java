package org.batfish.dataplane.protocols;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import org.batfish.datamodel.*;
import org.batfish.dataplane.exceptions.BgpRoutePropagationException;
import org.junit.Test;

public final class BgpProtocolHelperTest {

  /** Test that transformBgpRouteOnExport copies the tag from the input route. */
  @Test
  public void transformBgpRouteOnExport_setTag() throws BgpRoutePropagationException {
    NetworkFactory nf = new NetworkFactory();
    BgpActivePeerConfig fromNeighbor = nf.bgpNeighborBuilder().build();
    BgpPeerConfig toNeighbor = nf.bgpNeighborBuilder().build();
    BgpSessionProperties sessionProperties = BgpSessionProperties.from(fromNeighbor, toNeighbor);
    Ip fromVrfIp = new Ip("2.2.2.2");
    Vrf fromVrf = nf.vrfBuilder().build();
    nf.bgpProcessBuilder().setVrf(fromVrf).setRouterId(fromVrfIp).build();
    assertNotNull(fromVrf.getBgpProcess());
    Vrf toVrf = nf.vrfBuilder().build();
    AbstractRoute route =
        StaticRoute.builder().setNetwork(Prefix.parse("1.0.0.0/8")).setTag(12345).build();
    BgpRoute.Builder transformedRoute =
        BgpProtocolHelper.transformBgpRouteOnExport(
            fromNeighbor, toNeighbor, sessionProperties, fromVrf, toVrf, route);
    assertThat(transformedRoute.getTag(), equalTo(12345));
  }
}
