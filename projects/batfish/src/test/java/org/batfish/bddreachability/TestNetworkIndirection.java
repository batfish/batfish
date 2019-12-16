package org.batfish.bddreachability;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.util.SortedMap;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Configuration.Builder;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.PermittedByAcl;

/** A test network with a single node, with explicit ACL indirection. */
public final class TestNetworkIndirection {
  public static final Prefix IFACE_PREFIX = Prefix.parse("1.1.0.0/32");
  public static final Prefix LINK_NETWORK = Prefix.parse("1.0.0.0/31");
  public static final Ip IP_SPACE_IP_ADDR = Ip.parse("5.5.5.5");
  public static final String INDIRECT_ACL_NAME = "~Indirect_Acl~";
  public static final String INDIRECT_IPSPACE_NAME = "~Indirect_IpSpace~";

  public final SortedMap<String, Configuration> _configs;
  public final Interface _iface;
  public final Configuration _node;

  public TestNetworkIndirection() {
    NetworkFactory nf = new NetworkFactory();
    Builder cb = nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Interface.Builder ib = nf.interfaceBuilder().setActive(true).setBandwidth(1E9d);
    Vrf.Builder vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);

    _node = cb.build();
    Vrf dstVrf = vb.setOwner(_node).build();

    // indirect IP space used by the indirect ACL
    _node.setIpSpaces(ImmutableSortedMap.of(INDIRECT_IPSPACE_NAME, IP_SPACE_IP_ADDR.toIpSpace()));

    // indirect ACL used by the ingress ACL
    nf.aclBuilder()
        .setOwner(_node)
        .setLines(
            ImmutableList.of(
                ExprAclLine.acceptingHeaderSpace(
                    HeaderSpace.builder()
                        .setSrcIps(new IpSpaceReference(INDIRECT_IPSPACE_NAME))
                        .build())))
        .setName(INDIRECT_ACL_NAME)
        .build();

    IpAccessList link1DstIngressAcl =
        nf.aclBuilder()
            .setOwner(_node)
            .setLines(
                ImmutableList.of(
                    ExprAclLine.accepting()
                        .setMatchCondition(new PermittedByAcl(INDIRECT_ACL_NAME))
                        .build()))
            .build();

    ib.setAddress(
            ConcreteInterfaceAddress.create(
                LINK_NETWORK.getEndIp(), LINK_NETWORK.getPrefixLength()))
        .setIncomingFilter(link1DstIngressAcl)
        .setOwner(_node)
        .setVrf(dstVrf)
        .build();

    // unset incoming filter
    ib.setIncomingFilter(null);

    // destination
    _iface =
        ib.setOwner(_node)
            .setVrf(dstVrf)
            .setAddress(
                ConcreteInterfaceAddress.create(
                    IFACE_PREFIX.getStartIp(), IFACE_PREFIX.getPrefixLength()))
            .build();

    _configs = ImmutableSortedMap.of(_node.getHostname(), _node);
  }
}
