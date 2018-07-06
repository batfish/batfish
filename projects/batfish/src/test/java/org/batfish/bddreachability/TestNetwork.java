package org.batfish.bddreachability;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.SortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Configuration.Builder;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SourceNat;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Vrf;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.rules.TemporaryFolder;

/** A test network with two nodes and two static routes from one to the other. */
final class TestNetwork {
  static final Prefix DST_PREFIX_1 = Prefix.parse("1.1.0.0/32");
  static final Prefix DST_PREFIX_2 = Prefix.parse("2.1.0.0/32");
  static final Prefix LINK_1_NETWORK = Prefix.parse("1.0.0.0/31");
  static final Prefix LINK_2_NETWORK = Prefix.parse("2.0.0.0/31");
  static final Ip SOURCE_NAT_ACL_IP = new Ip("5.5.5.5");
  static final Ip SOURCE_NAT_POOL_IP = new Ip("6.6.6.6");
  static final int POST_SOURCE_NAT_ACL_DEST_PORT = 1234;

  final Batfish _batfish;
  final SortedMap<String, Configuration> _configs;
  final Interface _dstIface1;
  final Interface _dstIface2;
  final Configuration _dstNode;
  final Configuration _srcNode;
  final Interface _link1Src;
  final Interface _link2Src;
  final Interface _link1Dst;
  final Interface _link2Dst;
  final IpAccessList _link2SrcSourceNatAcl;

  TestNetwork() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Builder cb = nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Interface.Builder ib = nf.interfaceBuilder().setActive(true).setBandwidth(1E9d);
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);

    _srcNode = cb.build();
    _dstNode = cb.build();
    Vrf srcVrf = vb.setOwner(_srcNode).build();
    Vrf dstVrf = vb.setOwner(_dstNode).build();

    // first link
    _link1Src =
        ib.setOwner(_srcNode)
            .setVrf(srcVrf)
            .setAddress(
                new InterfaceAddress(LINK_1_NETWORK.getStartIp(), LINK_1_NETWORK.getPrefixLength()))
            .build();

    IpAccessList link1DstIngressAcl =
        nf.aclBuilder()
            .setOwner(_dstNode)
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.rejectingHeaderSpace(
                        HeaderSpace.builder().setDstIps(DST_PREFIX_2.toIpSpace()).build()),
                    IpAccessListLine.ACCEPT_ALL))
            .build();
    _link1Dst =
        ib.setAddress(
                new InterfaceAddress(LINK_1_NETWORK.getEndIp(), LINK_1_NETWORK.getPrefixLength()))
            .setIncomingFilter(link1DstIngressAcl)
            .setOwner(_dstNode)
            .setVrf(dstVrf)
            .build();

    // unset incoming filter
    ib.setIncomingFilter(null);

    // second link
    _link2SrcSourceNatAcl =
        nf.aclBuilder()
            .setOwner(_srcNode)
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.acceptingHeaderSpace(
                        HeaderSpace.builder().setSrcIps(SOURCE_NAT_ACL_IP.toIpSpace()).build())))
            .build();
    IpAccessList link2PostSourceNatAcl =
        nf.aclBuilder()
            .setOwner(_srcNode)
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.acceptingHeaderSpace(
                        HeaderSpace.builder()
                            .setDstPorts(
                                ImmutableList.of(new SubRange(POST_SOURCE_NAT_ACL_DEST_PORT)))
                            .build())))
            .build();
    _link2Src =
        ib.setAddress(
                new InterfaceAddress(LINK_2_NETWORK.getStartIp(), LINK_2_NETWORK.getPrefixLength()))
            .setSourceNats(
                ImmutableList.of(
                    SourceNat.builder()
                        .setAcl(_link2SrcSourceNatAcl)
                        .setPoolIpFirst(SOURCE_NAT_POOL_IP)
                        .setPoolIpLast(SOURCE_NAT_POOL_IP)
                        .build()))
            .setOutgoingFilter(link2PostSourceNatAcl)
            .setOwner(_srcNode)
            .setVrf(srcVrf)
            .build();

    ib.setSourceNats(null);
    ib.setOutgoingFilter(null);

    _link2Dst =
        ib.setAddress(
                new InterfaceAddress(LINK_2_NETWORK.getEndIp(), LINK_2_NETWORK.getPrefixLength()))
            .setOwner(_dstNode)
            .setVrf(dstVrf)
            .build();

    // destination for the first link
    _dstIface1 =
        ib.setOwner(_dstNode)
            .setVrf(dstVrf)
            .setAddress(
                new InterfaceAddress(DST_PREFIX_1.getStartIp(), DST_PREFIX_1.getPrefixLength()))
            .build();

    // destination for the second link
    _dstIface2 =
        ib.setOwner(_dstNode)
            .setVrf(dstVrf)
            .setAddress(
                new InterfaceAddress(DST_PREFIX_2.getStartIp(), DST_PREFIX_2.getPrefixLength()))
            .build();

    StaticRoute.Builder bld = StaticRoute.builder();
    srcVrf.setStaticRoutes(
        ImmutableSortedSet.of(
            bld.setNetwork(DST_PREFIX_1).setNextHopIp(LINK_1_NETWORK.getEndIp()).build(),
            bld.setNetwork(DST_PREFIX_2).setNextHopIp(LINK_1_NETWORK.getEndIp()).build(),
            bld.setNetwork(DST_PREFIX_2).setNextHopIp(LINK_2_NETWORK.getEndIp()).build()));

    _configs = ImmutableSortedMap.of(_srcNode.getName(), _srcNode, _dstNode.getName(), _dstNode);
    TemporaryFolder temp = new TemporaryFolder();
    temp.create();
    _batfish = BatfishTestUtils.getBatfish(_configs, temp);
  }
}
