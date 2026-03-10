package org.batfish.common.util.isp;

import static org.batfish.datamodel.matchers.IpAccessListMatchers.accepts;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.rejects;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.junit.Test;

/** Tests of {@link org.batfish.common.util.isp.BlockReservedAddressesAtInternet}. */
public class BlockReservedAddressesAtInternetTest {
  private void testMultipleFlows(IpAccessList acl) {
    Flow.Builder base =
        Flow.builder().setIngressNode("node").setIngressVrf("vrf").setIpProtocol(IpProtocol.OSPF);
    Ip pub1 = Ip.parse("8.8.8.8");
    Ip pub2 = Ip.parse("9.9.9.9");
    Ip pvt1 = Ip.parse("10.0.0.0");
    Ip pvt2 = Ip.parse("172.16.0.0");
    Flow publicToPublic = base.setSrcIp(pub1).setDstIp(pub2).build();
    Flow publicToPrivate = base.setSrcIp(pub1).setDstIp(pvt2).build();
    Flow privateToPublic = base.setSrcIp(pvt1).setDstIp(pub2).build();
    Flow privateToPrivate = base.setSrcIp(pvt1).setDstIp(pvt2).build();

    assertThat(acl, accepts(publicToPublic, null, ImmutableMap.of(), ImmutableMap.of()));
    assertThat(acl, rejects(publicToPrivate, null, ImmutableMap.of(), ImmutableMap.of()));
    assertThat(acl, rejects(privateToPublic, null, ImmutableMap.of(), ImmutableMap.of()));
    assertThat(acl, rejects(privateToPrivate, null, ImmutableMap.of(), ImmutableMap.of()));
  }

  @Test
  public void testFilters() {
    BlockReservedAddressesAtInternet policy = BlockReservedAddressesAtInternet.create();
    // From and to network are not present.
    assertThat(policy.filterTrafficFromNetwork(), nullValue());
    assertThat(policy.filterTrafficToNetwork(), nullValue());

    // From and to Internet are present and behave as expected.
    testMultipleFlows(policy.filterTrafficToInternet());
    testMultipleFlows(policy.filterTrafficFromInternet());
  }
}
