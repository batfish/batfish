package org.batfish.symbolic.smt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.SortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Configuration.Builder;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.rules.TemporaryFolder;

/** A test network with two nodes and two static routes from one to the other. */
public class TwoNodeNetworkWithTwoLinks {
  public static final Prefix DST_PREFIX_1 = Prefix.parse("1.1.0.0/32");
  public static final Prefix DST_PREFIX_2 = Prefix.parse("2.1.0.0/32");
  public static final Prefix LINK_1_NETWORK = Prefix.parse("1.0.0.0/31");
  public static final Prefix LINK_2_NETWORK = Prefix.parse("2.0.0.0/31");

  public final Batfish _batfish;
  public final Configuration _dstNode;
  public final Configuration _srcNode;

  public TwoNodeNetworkWithTwoLinks() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Builder cb = nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Interface.Builder ib = nf.interfaceBuilder().setActive(true).setBandwidth(1E9d);
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);

    _srcNode = cb.build();
    _dstNode = cb.build();
    Vrf srcVrf = vb.setOwner(_srcNode).build();
    Vrf dstVrf = vb.setOwner(_dstNode).build();

    // first link
    ib.setOwner(_srcNode)
        .setVrf(srcVrf)
        .setAddress(
            new InterfaceAddress(LINK_1_NETWORK.getStartIp(), LINK_1_NETWORK.getPrefixLength()))
        .build();
    ib.setOwner(_dstNode)
        .setVrf(dstVrf)
        .setAddress(
            new InterfaceAddress(LINK_1_NETWORK.getEndIp(), LINK_1_NETWORK.getPrefixLength()))
        .setSourceNats(ImmutableList.of())
        .build();

    // second link
    ib.setOwner(_srcNode)
        .setVrf(srcVrf)
        .setAddress(
            new InterfaceAddress(LINK_2_NETWORK.getStartIp(), LINK_2_NETWORK.getPrefixLength()))
        .build();
    ib.setOwner(_dstNode)
        .setVrf(dstVrf)
        .setAddress(
            new InterfaceAddress(LINK_2_NETWORK.getEndIp(), LINK_2_NETWORK.getPrefixLength()))
        .setSourceNats(ImmutableList.of())
        .build();

    // destination for the first link
    ib.setOwner(_dstNode)
        .setVrf(dstVrf)
        .setAddress(new InterfaceAddress(DST_PREFIX_1.getStartIp(), DST_PREFIX_1.getPrefixLength()))
        .build();

    // destination for the second link
    ib.setOwner(_dstNode)
        .setVrf(dstVrf)
        .setAddress(new InterfaceAddress(DST_PREFIX_2.getStartIp(), DST_PREFIX_2.getPrefixLength()))
        .build();

    StaticRoute.Builder bld = StaticRoute.builder();
    srcVrf.setStaticRoutes(
        ImmutableSortedSet.of(
            bld.setNetwork(DST_PREFIX_1).setNextHopIp(LINK_1_NETWORK.getEndIp()).build(),
            bld.setNetwork(DST_PREFIX_2).setNextHopIp(LINK_2_NETWORK.getEndIp()).build()));

    SortedMap<String, Configuration> configs =
        ImmutableSortedMap.of(_srcNode.getName(), _srcNode, _dstNode.getName(), _dstNode);
    TemporaryFolder temp = new TemporaryFolder();
    temp.create();
    _batfish = BatfishTestUtils.getBatfish(configs, temp);
  }
}
