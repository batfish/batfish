package org.batfish.datamodel;

import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.SortedMap;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class NewForwardingAnalysisImplTest {
  @Test
  public void testNeighborUnreachable_loopback31() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Interface.Builder ib = nf.interfaceBuilder().setActive(true).setBandwidth(1E9d);
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);

    Configuration node = cb.build();
    Vrf vrf = vb.setOwner(node).build();

    Prefix prefix = Prefix.parse("1.0.0.0/31");
    Interface iface =
        ib.setOwner(node)
            .setVrf(vrf)
            .setAddress(new InterfaceAddress(prefix.getStartIp(), prefix.getPrefixLength()))
            .build();

    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(node.getName(), node);

    TemporaryFolder temp = new TemporaryFolder();
    temp.create();

    Batfish batfish = BatfishTestUtils.getBatfish(configs, temp);
    batfish.computeDataPlane(false);
    DataPlane dataPlane = batfish.loadDataPlane();

    ForwardingAnalysis forwardingAnalysis = dataPlane.getForwardingAnalysis();

    IpSpace ipSpace =
        forwardingAnalysis
            .getNeighborUnreachable()
            .get(node.getName())
            .get(vrf.getName())
            .get(iface.getName());

    assertThat(ipSpace, containsIp(prefix.getEndIp()));
    assertThat(ipSpace, not(containsIp(prefix.getStartIp())));
  }

  @Test
  public void testNeighborUnreachable_loopback32() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Interface.Builder ib = nf.interfaceBuilder().setActive(true).setBandwidth(1E9d);
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);

    Configuration node = cb.build();
    Vrf vrf = vb.setOwner(node).build();

    Prefix prefix = Prefix.parse("1.0.0.0/32");
    Interface iface =
        ib.setOwner(node)
            .setVrf(vrf)
            .setAddress(new InterfaceAddress(prefix.getStartIp(), prefix.getPrefixLength()))
            .build();

    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(node.getName(), node);

    TemporaryFolder temp = new TemporaryFolder();
    temp.create();

    Batfish batfish = BatfishTestUtils.getBatfish(configs, temp);
    batfish.computeDataPlane(false);
    DataPlane dataPlane = batfish.loadDataPlane();

    ForwardingAnalysis forwardingAnalysis = dataPlane.getForwardingAnalysis();

    IpSpace ipSpace =
        forwardingAnalysis
            .getNeighborUnreachable()
            .get(node.getName())
            .get(vrf.getName())
            .get(iface.getName());

    assertThat(ipSpace, not(containsIp(prefix.getStartIp())));
  }
}
