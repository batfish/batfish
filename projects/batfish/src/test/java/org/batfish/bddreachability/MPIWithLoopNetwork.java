package org.batfish.bddreachability;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedMap;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Configuration.Builder;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;

public class MPIWithLoopNetwork {

  public static SortedMap<String, Configuration> testMPIWithLoopNetwork() {
    NetworkFactory nf = new NetworkFactory();
    Builder cb = nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    // first node
    Configuration c1 = cb.setHostname("configuration1").build();
    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();
    ConcreteInterfaceAddress c1Addr1 = ConcreteInterfaceAddress.parse("1.0.0.0/31");
    Interface i11 =
        nf.interfaceBuilder().setActive(true).setOwner(c1).setVrf(v1).setAddress(c1Addr1).build();
    ConcreteInterfaceAddress c1Addr2 = ConcreteInterfaceAddress.parse("1.0.0.2/31");
    Interface i12 =
        nf.interfaceBuilder().setActive(true).setOwner(c1).setVrf(v1).setAddress(c1Addr2).build();

    // second node
    Configuration c2 = cb.setHostname("configuration2").build();
    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();
    ConcreteInterfaceAddress c2Addr = ConcreteInterfaceAddress.parse("1.0.0.1/31");
    Interface i2 =
        nf.interfaceBuilder().setActive(true).setOwner(c2).setVrf(v2).setAddress(c2Addr).build();

    // third node
    Configuration c3 = cb.setHostname("configuration3").build();
    Vrf v3 = nf.vrfBuilder().setOwner(c3).build();
    ConcreteInterfaceAddress c3Addr1 = ConcreteInterfaceAddress.parse("1.0.0.3/31");
    nf.interfaceBuilder().setActive(true).setOwner(c3).setVrf(v3).setAddress(c3Addr1).build();
    ConcreteInterfaceAddress c3Addr2 = ConcreteInterfaceAddress.parse("2.2.2.2/32");
    nf.interfaceBuilder().setActive(true).setOwner(c3).setVrf(v3).setAddress(c3Addr2).build();

    // we want to get to c3
    Prefix targetPrefix = Prefix.parse("2.2.2.2/32");

    // routing to both c1 and c3 to add inconsistency
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(targetPrefix)
                .setAdministrativeCost(1)
                .setNextHopInterface(i11.getName())
                .setNextHopIp(c2Addr.getIp())
                .build(),
            StaticRoute.testBuilder()
                .setNetwork(targetPrefix)
                .setAdministrativeCost(1)
                .setNextHopInterface(i12.getName())
                .setNextHopIp(c3Addr1.getIp())
                .build()));

    // looping back the packet back to c1 if we are at c2
    v2.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(targetPrefix)
                .setAdministrativeCost(1)
                .setNextHopInterface(i2.getName())
                .setNextHopIp(c1Addr1.getIp())
                .build()));

    return ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2, c3.getHostname(), c3);
  }
}
