package org.batfish.engine;

import com.google.common.collect.ImmutableSortedMap;
import java.util.SortedMap;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.batfish.datamodel.ospf.OspfProcess;

/**
 * A hand-built network: r1 and r2 joined by Ethernet1 on 10.0.12.0/30, each with a loopback, all in
 * OSPF area 0. Each router should learn the other's loopback over OSPF.
 */
final class TwoRouterOspf {

  static final String R1 = "r1";
  static final String R2 = "r2";
  static final String R1_LOOPBACK = "1.1.1.1/32";
  static final String R2_LOOPBACK = "2.2.2.2/32";

  private TwoRouterOspf() {}

  /** Fresh, unfinalized models on every call; finalization mutates them. */
  static SortedMap<String, Configuration> build() {
    NetworkFactory nf = new NetworkFactory();
    return ImmutableSortedMap.of(
        R1, router(nf, R1, "1.1.1.1", "10.0.12.1/30"),
        R2, router(nf, R2, "2.2.2.2", "10.0.12.2/30"));
  }

  private static Configuration router(
      NetworkFactory nf, String hostname, String routerId, String linkAddress) {
    Configuration c =
        nf.configurationBuilder()
            .setHostname(hostname)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .setDefaultInboundAction(LineAction.PERMIT)
            .build();
    Vrf vrf = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(c).build();
    OspfProcess process =
        nf.ospfProcessBuilder()
            .setProcessId("1")
            .setRouterId(Ip.parse(routerId))
            .setVrf(vrf)
            .build();
    OspfArea area = nf.ospfAreaBuilder().setOspfProcess(process).setNumber(0L).setNonStub().build();
    OspfInterfaceSettings ospf =
        OspfInterfaceSettings.defaultSettingsBuilder()
            .setCost(10)
            .setProcess("1")
            .setAreaName(0L)
            .build();
    Interface.Builder ib =
        nf.interfaceBuilder().setOwner(c).setVrf(vrf).setBandwidth(1e9).setOspfSettings(ospf);
    ib.setName("Ethernet1")
        .setType(InterfaceType.PHYSICAL)
        .setAddress(ConcreteInterfaceAddress.parse(linkAddress))
        .build();
    ib.setName("Loopback0")
        .setType(InterfaceType.LOOPBACK)
        .setAddress(ConcreteInterfaceAddress.parse(routerId + "/32"))
        .build();
    area.addInterface("Ethernet1");
    area.addInterface("Loopback0");
    return c;
  }
}
