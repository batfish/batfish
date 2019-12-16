package org.batfish.bddreachability;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.util.SortedMap;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.OriginatingFromDevice;

/**
 * A test network with a single node that exercises {@link
 * org.batfish.datamodel.acl.MatchSrcInterface} and {@link
 * org.batfish.datamodel.acl.OriginatingFromDevice}.
 */
public class TestNetworkSources {
  /* Properties of the primary config, which has the ACLs that use MatchSrcInterface and
   * OriginatingFromDevice.
   */
  private static final ConcreteInterfaceAddress CONFIG_ADDR =
      ConcreteInterfaceAddress.parse("0.0.0.1/24");
  static final String CONFIG_NAME = "config";
  static final String INGRESS_IFACE_NAME = "ingress interface";
  static final String MATCH_SRC_INTERFACE_ACL_IFACE_NAME = "interface with match src interface ACL";
  static final String ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME =
      "interface with originating from device ACL";

  /*
   * Properties of the peer config that just forwards traffic to the primary config.
   */
  static final String PEER_IFACE_NAME = "peer interface";
  static final String PEER_NAME = "peer";
  static final ConcreteInterfaceAddress PEER_ADDR = ConcreteInterfaceAddress.parse("0.0.0.2/24");

  /*
   * Properties shared by both nodes
   */
  static final String VRF_NAME = "vrf";

  /**
   * Builds the config with ACLs that use {@link org.batfish.datamodel.acl.MatchSrcInterface} and
   * {@link OriginatingFromDevice} in the test networks.
   */
  private static Configuration makeConfigWithAcls(NetworkFactory nf) {
    Configuration config =
        nf.configurationBuilder()
            .setHostname(CONFIG_NAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Vrf vrf = nf.vrfBuilder().setName(VRF_NAME).setOwner(config).build();
    Interface.Builder ib = nf.interfaceBuilder().setActive(true).setOwner(config).setVrf(vrf);

    Interface inInterface = ib.setName(INGRESS_IFACE_NAME).setAddress(CONFIG_ADDR).build();

    // only traffic originating from device can go out this interface
    IpAccessList acl1 =
        nf.aclBuilder()
            .setOwner(config)
            .setLines(ImmutableList.of(ExprAclLine.accepting(OriginatingFromDevice.INSTANCE)))
            .build();
    ib.setName(ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME)
        .setOutgoingFilter(acl1)
        .setAddress(ConcreteInterfaceAddress.create(Ip.parse("1.0.0.1"), 24))
        .build();

    // only traffic from inInterface from device can go out this interface
    IpAccessList acl2 =
        nf.aclBuilder()
            .setOwner(config)
            .setLines(
                ImmutableList.of(
                    ExprAclLine.accepting(
                        AclLineMatchExprs.matchSrcInterface(inInterface.getName()))))
            .build();
    ib.setName(MATCH_SRC_INTERFACE_ACL_IFACE_NAME)
        .setOutgoingFilter(acl2)
        .setAddress(ConcreteInterfaceAddress.create(Ip.parse("2.0.0.1"), 24))
        .build();

    return config;
  }

  private static Configuration makePeerConfig(NetworkFactory nf) {
    Configuration config =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname(PEER_NAME)
            .build();
    Vrf vrf = nf.vrfBuilder().setName(VRF_NAME).setOwner(config).build();
    nf.interfaceBuilder()
        .setActive(true)
        .setOwner(config)
        .setName(PEER_IFACE_NAME)
        .setVrf(vrf)
        .setAddress(PEER_ADDR)
        .build();
    return config;
  }

  /** @return A one-node network. For testing traffic originating from the device or its links. */
  public static SortedMap<String, Configuration> oneNodeNetwork() {
    Configuration config = makeConfigWithAcls(new NetworkFactory());
    return ImmutableSortedMap.of(config.getHostname(), config);
  }

  public static SortedMap<String, Configuration> twoNodeNetwork() {
    NetworkFactory nf = new NetworkFactory();
    Configuration configWithAcls = makeConfigWithAcls(nf);
    Configuration peerConfig = makePeerConfig(nf);
    return ImmutableSortedMap.of(
        configWithAcls.getHostname(), configWithAcls, peerConfig.getHostname(), peerConfig);
  }
}
