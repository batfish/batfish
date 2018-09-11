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
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.PermittedByAcl;

/** A test network with two nodes and two static routes from one to the other. */
public final class TestNetworkIndirection {
  public static final Prefix DST_PREFIX = Prefix.parse("1.1.0.0/32");
  public static final Prefix LINK_NETWORK = Prefix.parse("1.0.0.0/31");
  public static final Ip IP_SPACE_IP_ADDR = new Ip("5.5.5.5");
  public static final String INDIRECT_ACL_NAME = "~Indirect_Acl~";

  public final SortedMap<String, Configuration> _configs;
  public final Interface _dstIface1;
  public final Configuration _dstNode;
  public final Configuration _srcNode;
  public final Interface _linkSrc;
  public final Interface _linkDst;

  public TestNetworkIndirection() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Builder cb = nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Interface.Builder ib = nf.interfaceBuilder().setActive(true).setBandwidth(1E9d);
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);

    _srcNode = cb.build();
    _dstNode = cb.build();
    Vrf srcVrf = vb.setOwner(_srcNode).build();
    Vrf dstVrf = vb.setOwner(_dstNode).build();

    _linkSrc =
        ib.setOwner(_srcNode)
            .setVrf(srcVrf)
            .setAddress(
                new InterfaceAddress(LINK_NETWORK.getStartIp(), LINK_NETWORK.getPrefixLength()))
            .build();

    // indirect ACL used by the following ACL
    nf.aclBuilder()
        .setOwner(_dstNode)
        .setLines(
            ImmutableList.of(
                IpAccessListLine.acceptingHeaderSpace(
                    HeaderSpace.builder().setSrcIps(IP_SPACE_IP_ADDR.toIpSpace()).build())))
        .setName(INDIRECT_ACL_NAME)
        .build();

    IpAccessList link1DstIngressAcl =
        nf.aclBuilder()
            .setOwner(_dstNode)
            .setLines(
                ImmutableList.of(
                    IpAccessListLine.accepting()
                        .setMatchCondition(new PermittedByAcl(INDIRECT_ACL_NAME))
                        .build()))
            .build();

    _linkDst =
        ib.setAddress(new InterfaceAddress(LINK_NETWORK.getEndIp(), LINK_NETWORK.getPrefixLength()))
            .setIncomingFilter(link1DstIngressAcl)
            .setOwner(_dstNode)
            .setVrf(dstVrf)
            .build();

    // unset incoming filter
    ib.setIncomingFilter(null);

    // destination
    _dstIface1 =
        ib.setOwner(_dstNode)
            .setVrf(dstVrf)
            .setAddress(new InterfaceAddress(DST_PREFIX.getStartIp(), DST_PREFIX.getPrefixLength()))
            .build();

    StaticRoute.Builder bld = StaticRoute.builder();
    srcVrf.setStaticRoutes(
        ImmutableSortedSet.of(
            bld.setNetwork(DST_PREFIX).setNextHopIp(LINK_NETWORK.getEndIp()).build()));

    _configs =
        ImmutableSortedMap.of(_srcNode.getHostname(), _srcNode, _dstNode.getHostname(), _dstNode);
  }
}
