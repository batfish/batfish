package org.batfish.dataplane.ibdp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import com.google.common.collect.Table;
import java.util.List;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.ConcreteInterfaceAddress6;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute6;
import org.batfish.datamodel.FinalMainRib6;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Prefix6;
import org.junit.Test;

/** Tests IPv6 main-RIB preservation in the completed dataplane. */
public final class IncrementalDataPlaneIpv6Test {

  @Test
  public void testComputeRibs6() {
    Node node =
        TestUtils.makeIosRouter("n1");

    Configuration c =
        node.getConfiguration();

    Interface.builder()
        .setName("Ethernet1")
        .setOwner(c)
        .setVrf(c.getDefaultVrf())
        .setType(InterfaceType.PHYSICAL)
        .setAddress(
            ConcreteInterfaceAddress6.parse(
                "2001:db8:10::1/64"))
        .build();

    VirtualRouter vr =
        node.getVirtualRouterOrThrow(
            Configuration.DEFAULT_VRF_NAME);

    vr.initForIgpComputation(
        TopologyContext.builder().build());

    Table<String, String, FinalMainRib6>
        ribs =
            IncrementalDataPlane.computeRibs6(
                List.of(vr));

    FinalMainRib6 rib =
        ribs.get(
            "n1",
            Configuration.DEFAULT_VRF_NAME);

    assertThat(rib, notNullValue());

    Prefix6 prefix =
        Prefix6.parse(
            "2001:db8:10::/64");

    assertThat(
        rib.getRoutes(prefix).size(),
        equalTo(1));

    AbstractRoute6 route =
        rib.getRoutes(prefix)
            .iterator()
            .next();

    assertThat(
        route instanceof ConnectedRoute6,
        equalTo(true));

    assertThat(
        rib.getRoutes(),
        hasItem(route));
  }
}
