package org.batfish.representation.fortios;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.batfish.datamodel.Vrf;

/** Helper functions for generating VI IPsec VPN structures for {@link FortiosConfiguration}. */
public final class FortiosIpsecConversions {

  /** Converts FortiOS IPsec Phase 1 and Phase 2 configurations to VI model. */
  public static void convertIpsec(
      @Nonnull Map<String, IpsecPhase1> phase1Configs,
      @Nonnull Map<String, IpsecPhase2> phase2Configs,
      Configuration c,
      Warnings w) {

    if (phase1Configs.isEmpty()) {
      return;
    }

    for (IpsecPhase1 phase1 : phase1Configs.values()) {
      convertIpsecPhase1(phase1, c, w);
    }
  }

  private static void convertIpsecPhase1(IpsecPhase1 phase1, Configuration c, Warnings w) {
    String name = phase1.getName();

    // Remote gateway is required
    Ip remoteGateway = phase1.getRemoteGateway();
    if (remoteGateway == null) {
      w.redFlagf("IPsec Phase 1 %s has no remote gateway configured", name);
      return;
    }

    // Source interface is required
    String sourceInterface = phase1.getInterface();
    if (sourceInterface == null) {
      w.redFlagf("IPsec Phase 1 %s has no interface configured", name);
      return;
    }

    // Look up the source interface
    Interface viSourceInterface = c.getAllInterfaces().get(sourceInterface);
    if (viSourceInterface == null) {
      w.redFlagf("IPsec Phase 1 %s references non-existent interface %s", name, sourceInterface);
      return;
    }

    // Get local address from the source interface
    Ip localAddress = null;
    if (viSourceInterface.getConcreteAddress() != null) {
      localAddress = viSourceInterface.getConcreteAddress().getIp();
    }
    if (localAddress == null) {
      w.redFlagf("IPsec Phase 1 %s: source interface %s has no IP address", name, sourceInterface);
      return;
    }

    // Create a tunnel interface for this VPN
    String tunnelInterfaceName = "tunnel-" + name;
    Vrf defaultVrf = c.getDefaultVrf();

    Interface.Builder tunnelIfaceBuilder =
        Interface.builder()
            .setName(tunnelInterfaceName)
            .setOwner(c)
            .setVrf(defaultVrf)
            .setType(InterfaceType.TUNNEL)
            .setAdminUp(true)
            .setDescription("IPsec tunnel: " + name);

    // Build the tunnel interface
    tunnelIfaceBuilder.build();

    // Create IpsecStaticPeerConfig
    IpsecStaticPeerConfig ipsecConfig =
        IpsecStaticPeerConfig.builder()
            .setTunnelInterface(tunnelInterfaceName)
            .setDestinationAddress(remoteGateway)
            .setLocalAddress(localAddress)
            .setSourceInterface(sourceInterface)
            .build();

    c.getIpsecPeerConfigs().put(tunnelInterfaceName, ipsecConfig);
  }

  private FortiosIpsecConversions() {}
}
