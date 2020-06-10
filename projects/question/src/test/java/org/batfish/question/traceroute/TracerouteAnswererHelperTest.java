package org.batfish.question.traceroute;

import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressInterface;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressNode;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIpProtocol;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.Vrf;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.specifier.AllInterfacesLocationSpecifier;
import org.batfish.specifier.InterfaceLinkLocation;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;
import org.batfish.specifier.MockSpecifierContext;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierContextImpl;
import org.batfish.specifier.SpecifierFactories;
import org.junit.Test;

public class TracerouteAnswererHelperTest {
  @Test
  public void testGetFlows() {
    NetworkFactory nf = new NetworkFactory();
    Configuration config =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().build();
    Interface.Builder ifaceBuilder = nf.interfaceBuilder().setOwner(config).setVrf(vrf);
    Interface activeIface = ifaceBuilder.setActive(true).build();
    Interface inactiveIface = ifaceBuilder.setActive(false).build();

    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(config.getHostname(), config);

    IBatfish batfish =
        new IBatfishTestAdapter() {
          @Override
          public SortedMap<String, Configuration> loadConfigurations(NetworkSnapshot snapshot) {
            return configs;
          }

          @Override
          public Map<Location, LocationInfo> getLocationInfo(NetworkSnapshot networkSnapshot) {
            return ImmutableMap.of();
          }
        };

    SpecifierContextImpl ctxt =
        new SpecifierContextImpl(
            batfish, new NetworkSnapshot(new NetworkId("network"), new SnapshotId("snapshot")));

    PacketHeaderConstraints headerConstraints =
        PacketHeaderConstraints.builder().setSrcIp("1.1.1.1").setDstIp("2.2.2.2").build();
    String sourceLocationStr = "enter(/.*/)";

    // specifier resolves to locations of active and inactive interfaces
    Set<Location> specifiedLocations =
        SpecifierFactories.getLocationSpecifierOrDefault(
                sourceLocationStr, AllInterfacesLocationSpecifier.INSTANCE)
            .resolve(ctxt);
    assertThat(
        specifiedLocations,
        containsInAnyOrder(
            new InterfaceLinkLocation(config.getHostname(), activeIface.getName()),
            new InterfaceLinkLocation(config.getHostname(), inactiveIface.getName())));

    // getFlows filters out locations of inactive interfaces
    TracerouteAnswererHelper helper =
        new TracerouteAnswererHelper(headerConstraints, sourceLocationStr, ctxt);

    Set<Flow> flows = helper.getFlows();
    assertThat(
        flows,
        contains(
            allOf(
                hasIngressNode(config.getHostname()), hasIngressInterface(activeIface.getName()))));
  }

  @Test
  public void testGetFlows_ICMP() {
    NetworkFactory nf = new NetworkFactory();
    Configuration config =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("node")
            .build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    Interface iface =
        nf.interfaceBuilder().setVrf(vrf).setOwner(config).setActive(true).setName("iface").build();
    SpecifierContext ctxt =
        MockSpecifierContext.builder()
            .setConfigs(ImmutableMap.of(config.getHostname(), config))
            .setLocationInfo(
                ImmutableMap.of(
                    Location.interfaceLocation(iface),
                    new LocationInfo(true, Ip.parse("1.1.1.1").toIpSpace(), EmptyIpSpace.INSTANCE)))
            .build();
    PacketHeaderConstraints phc =
        PacketHeaderConstraints.builder().setDstIp("8.8.8.8").setApplications("icmp/3/15").build();
    Set<Flow> flows = new TracerouteAnswererHelper(phc, ".*", ctxt).getFlows();
    assertThat(flows, contains(allOf(hasIpProtocol(IpProtocol.ICMP))));
  }
}
