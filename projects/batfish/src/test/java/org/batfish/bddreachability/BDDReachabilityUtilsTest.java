package org.batfish.bddreachability;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.junit.Test;

/** Tests for {@link BDDReachabilityUtils}. */
public class BDDReachabilityUtilsTest {

  @Test
  public void computeIpsNotOwnedByInactiveInterfaces() {
    NetworkFactory nf = new NetworkFactory();

    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration node = cb.build();
    Vrf vrf = nf.vrfBuilder().setOwner(node).build();
    Interface.Builder ib = nf.interfaceBuilder().setOwner(node).setVrf(vrf);
    ib.setName("lo0").setAddress(new InterfaceAddress("1.1.1.1/24")).build();
    ib.setName("lo1").setAddress(new InterfaceAddress("2.2.2.2/24")).setActive(false).build();

    Map<String, Configuration> configs = ImmutableMap.of(node.getHostname(), node);
    Map<String, Map<String, IpSpace>> result =
        BDDReachabilityUtils.computeIpsNotOwnedByInactiveInterfaces(configs);
    assertEquals(
        result,
        ImmutableMap.of(
            node.getHostname(),
            ImmutableMap.of(vrf.getName(), Ip.parse("2.2.2.2").toIpSpace().complement())));
  }
}
