package org.batfish.datamodel.topology;

import static org.batfish.common.topology.bridge_domain.edge.L2VniToVlanAwareBridgeDomain.l2VniToVlanAwareBridgeDomain;
import static org.batfish.common.topology.bridge_domain.edge.NonVlanAwareBridgeDomainToL2Vni.instance;
import static org.batfish.common.topology.bridge_domain.edge.VlanAwareBridgeDomainToL2Vni.vlanAwareBridgeDomainToL2Vni;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.L2ToNonVlanAwareBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.NonVlanAwareBridgeDomainToL2;
import org.batfish.common.topology.bridge_domain.node.BridgeDomain;
import org.batfish.common.topology.bridge_domain.node.L1Interface;
import org.batfish.common.topology.bridge_domain.node.L2Interface;
import org.batfish.common.topology.bridge_domain.node.L2Vni;
import org.batfish.common.topology.bridge_domain.node.L3BridgedInterface;
import org.batfish.common.topology.bridge_domain.node.L3DisconnectedInterface;
import org.batfish.common.topology.bridge_domain.node.L3Interface;
import org.batfish.common.topology.bridge_domain.node.L3NonBridgedInterface;
import org.batfish.common.topology.bridge_domain.node.NonVlanAwareBridgeDomain;
import org.batfish.common.topology.bridge_domain.node.VlanAwareBridgeDomain;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.datamodel.vxlan.VniLayer;
import org.batfish.datamodel.vxlan.VxlanNode;

/** Helper functions for computing device topology. */
public final class DeviceTopologyUtils {

  /**
   * Compute the {@link DeviceTopology} for a configuration based on {@link InterfaceTopology}s of
   * active interfaces; and {@link Layer2Vni}s.
   */
  public static @Nonnull DeviceTopology computeDeviceTopology(Configuration c) {
    String hostname = c.getHostname();
    Map<String, Interface> activeInterfaces = c.getActiveInterfaces();
    Map<String, InterfaceTopology> interfaceTopologies =
        computeInterfaceTopologies(activeInterfaces);
    Map<Integer, Layer2Vni> configLayer2Vnis =
        c.getVrfs().values().stream()
            .map(Vrf::getLayer2Vnis)
            .filter(layer2Vnis -> !layer2Vnis.isEmpty())
            // Only the VRF that may have an EVPN routing process (typically the default VRF) may
            // have layer-2 VNIs. This should be enforced in conversion.
            .findFirst()
            .orElse(ImmutableMap.of());

    // compute nodes
    Map<String, L1Interface> l1Interfaces = computeL1Interfaces(hostname, interfaceTopologies);
    Map<String, NonVlanAwareBridgeDomain> nonVlanAwareBridgeDomains =
        computeNonVlanAwareBridgeDomains(hostname, interfaceTopologies, configLayer2Vnis);
    Map<String, VlanAwareBridgeDomain> vlanAwareBridgeDomains =
        computeVlanAwareBridgeDomains(hostname, interfaceTopologies, configLayer2Vnis);
    Map<String, L2Interface> l2Interfaces = computeL2Interfaces(hostname, interfaceTopologies);

    Map<Integer, L2Vni> l2Vnis = computeL2Vnis(hostname, configLayer2Vnis);
    Map<String, L3Interface> l3Interfaces = computeL3Interfaces(hostname, interfaceTopologies);

    // compute edges
    computeL2InterfaceConnections(
        interfaceTopologies,
        l2Interfaces,
        l1Interfaces,
        nonVlanAwareBridgeDomains,
        vlanAwareBridgeDomains);
    computeL3InterfaceConnections(
        interfaceTopologies,
        l3Interfaces,
        l1Interfaces,
        nonVlanAwareBridgeDomains,
        vlanAwareBridgeDomains);
    computeL2VniConnections(
        configLayer2Vnis, l2Vnis, nonVlanAwareBridgeDomains, vlanAwareBridgeDomains);

    return DeviceTopology.of(
        l1Interfaces,
        l2Interfaces,
        l2Vnis,
        l3Interfaces,
        vlanAwareBridgeDomains,
        nonVlanAwareBridgeDomains);
  }

  /** Return map from interface name -> interface's topology */
  private static @Nonnull Map<String, InterfaceTopology> computeInterfaceTopologies(
      Map<String, Interface> activeInterfaces) {
    return toImmutableMap(activeInterfaces, Entry::getKey, e -> e.getValue().getOrLegacyTopology());
  }

  /**
   * Creates {@link L1Interface}s for those {@link Interface}s for which {@link
   * InterfaceTopology#isLogicalLayer1()} is {@code true}.
   *
   * <p>Returns map from interface name -> l1 interface
   */
  private static @Nonnull Map<String, L1Interface> computeL1Interfaces(
      String hostname, Map<String, InterfaceTopology> interfaceTopologies) {
    return interfaceTopologies.entrySet().stream()
        .filter(e -> e.getValue().isLogicalLayer1())
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey, e -> new L1Interface(NodeInterfacePair.of(hostname, e.getKey()))));
  }

  /**
   * Create {@link NonVlanAwareBridgeDomain}s for all those referenced in in {@link
   * Layer2NonVlanAwareBridgeSettings#getNonVlanAwareBridgeDomain()}, {@link
   * Layer3NonVlanAwareBridgeSettings#getNonVlanAwareBridge()}, and {@link
   * Layer2Vni#getNonVlanAwareBridgeDomain()}.
   *
   * <p>Returns map: name -> bridge domain
   */
  private static @Nonnull Map<String, NonVlanAwareBridgeDomain> computeNonVlanAwareBridgeDomains(
      String hostname,
      Map<String, InterfaceTopology> interfaceTopologies,
      Map<Integer, Layer2Vni> configLayer2Vnis) {
    ImmutableSet.Builder<String> names = ImmutableSet.builder();
    for (InterfaceTopology topology : interfaceTopologies.values()) {
      if (topology.getLayer2Settings().isPresent()) {
        topology.getLayer2Settings().get().getBridgeSettings().stream()
            .filter(Layer2NonVlanAwareBridgeSettings.class::isInstance)
            .map(Layer2NonVlanAwareBridgeSettings.class::cast)
            .map(Layer2NonVlanAwareBridgeSettings::getNonVlanAwareBridgeDomain)
            .forEach(names::add);
      }
      topology
          .getLayer3Settings()
          .filter(Layer3NonVlanAwareBridgeSettings.class::isInstance)
          .map(Layer3NonVlanAwareBridgeSettings.class::cast)
          .map(Layer3NonVlanAwareBridgeSettings::getNonVlanAwareBridge)
          .ifPresent(names::add);
    }
    for (Layer2Vni l2Vni : configLayer2Vnis.values()) {
      Optional<String> maybeName = l2Vni.getNonVlanAwareBridgeDomain();
      if (!maybeName.isPresent()) {
        continue;
      }
      names.add(maybeName.get());
    }
    return names.build().stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Function.identity(),
                name -> new NonVlanAwareBridgeDomain(BridgeDomain.Id.of(hostname, name))));
  }

  /**
   * Create {@link VlanAwareBridgeDomain}s for all those referenced in in {@link
   * Layer2VlanAwareBridgeSettings#getVlanAwareBridgeDomain()}, {@link
   * Layer3VlanAwareBridgeSettings#getVlanAwareBridge()}, and {@link
   * Layer2Vni#getVlanAwareBridgeDomain()}.
   *
   * <p>Returns map: name -> bridge domain
   */
  private static @Nonnull Map<String, VlanAwareBridgeDomain> computeVlanAwareBridgeDomains(
      String hostname,
      Map<String, InterfaceTopology> interfaceTopologies,
      Map<Integer, Layer2Vni> configLayer2Vnis) {
    ImmutableSet.Builder<String> names = ImmutableSet.builder();
    for (InterfaceTopology topology : interfaceTopologies.values()) {
      if (topology.getLayer2Settings().isPresent()) {
        topology.getLayer2Settings().get().getBridgeSettings().stream()
            .filter(Layer2VlanAwareBridgeSettings.class::isInstance)
            .map(Layer2VlanAwareBridgeSettings.class::cast)
            .map(Layer2VlanAwareBridgeSettings::getVlanAwareBridgeDomain)
            .forEach(names::add);
      }
      topology
          .getLayer3Settings()
          .filter(Layer3VlanAwareBridgeSettings.class::isInstance)
          .map(Layer3VlanAwareBridgeSettings.class::cast)
          .map(Layer3VlanAwareBridgeSettings::getVlanAwareBridge)
          .ifPresent(names::add);
    }
    for (Layer2Vni l2Vni : configLayer2Vnis.values()) {
      Optional<String> maybeName = l2Vni.getVlanAwareBridgeDomain();
      if (!maybeName.isPresent()) {
        continue;
      }
      names.add(maybeName.get());
    }
    return names.build().stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Function.identity(),
                name -> new VlanAwareBridgeDomain(BridgeDomain.Id.of(hostname, name))));
  }

  /**
   * Create {@link L2Interface}s for each {@link Interface} with {@link Layer2Settings}.
   *
   * <p>Returns map from interface name -> l2 interface.
   */
  private static @Nonnull Map<String, L2Interface> computeL2Interfaces(
      String hostname, Map<String, InterfaceTopology> interfaceTopologies) {
    return interfaceTopologies.entrySet().stream()
        .filter(e -> e.getValue().getLayer2Settings().isPresent())
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey, e -> new L2Interface(NodeInterfacePair.of(hostname, e.getKey()))));
  }

  /**
   * Create {@link L2Vni}s for each {@link Layer2Vni}.
   *
   * <p>Returns map from VNI number -> l2vni.
   */
  private static @Nonnull Map<Integer, L2Vni> computeL2Vnis(
      String hostname, Map<Integer, Layer2Vni> layer2Vnis) {
    return layer2Vnis.keySet().stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Function.identity(),
                vni -> L2Vni.of(new VxlanNode(hostname, vni, VniLayer.LAYER_2))));
  }

  /**
   * Create {@link L3Interface}s for each {@link Interface} with {@link Layer3Settings}.
   *
   * <p>Returns map from interface name -> l2 interface.
   */
  private static @Nonnull Map<String, L3Interface> computeL3Interfaces(
      String hostname, Map<String, InterfaceTopology> interfaceTopologies) {
    ImmutableMap.Builder<String, L3Interface> builder = ImmutableMap.builder();
    for (Entry<String, InterfaceTopology> e : interfaceTopologies.entrySet()) {
      e.getValue()
          .getLayer3Settings()
          .ifPresent(
              l3 ->
                  builder.put(
                      e.getKey(),
                      TO_L3_INTERFACE.visit(l3, NodeInterfacePair.of(hostname, e.getKey()))));
    }
    return builder.build();
  }

  /**
   * Visitor of {@link Layer3Settings} that creates an {@link L3Interface} based on settings
   * sub-type.
   */
  private static final Layer3SettingsArgVisitor<L3Interface, NodeInterfacePair> TO_L3_INTERFACE =
      new Layer3SettingsArgVisitor<L3Interface, NodeInterfacePair>() {
        @Override
        public L3Interface visitLayer3NonBridgedSettings(
            Layer3NonBridgedSettings layer3NonBridgedSettings, @Nullable NodeInterfacePair arg) {
          return new L3NonBridgedInterface(arg);
        }

        @Override
        public L3Interface visitLayer3NonVlanAwareBridgeSettings(
            Layer3NonVlanAwareBridgeSettings layer3NonVlanAwareBridgeSettings,
            @Nullable NodeInterfacePair arg) {
          return new L3BridgedInterface(arg);
        }

        @Override
        public L3Interface visitLayer3VlanAwareBridgeSettings(
            Layer3VlanAwareBridgeSettings layer3VlanAwareBridgeSettings,
            @Nullable NodeInterfacePair arg) {
          return new L3BridgedInterface(arg);
        }

        @Override
        public L3Interface visitLayer3TunnelSettings(
            Layer3TunnelSettings layer3TunnelSettings, @Nullable NodeInterfacePair arg) {
          return new L3DisconnectedInterface(arg);
        }
      };

  /**
   * Populate edges between {@link L2Interface}s and {@link L1Interface}s; and {@link L2Interface}s
   * and {@link BridgeDomain}s.
   */
  private static void computeL2InterfaceConnections(
      Map<String, InterfaceTopology> interfaceTopologies,
      Map<String, L2Interface> l2Interfaces,
      Map<String, L1Interface> l1Interfaces,
      Map<String, NonVlanAwareBridgeDomain> nonVlanAwareBridgeDomains,
      Map<String, VlanAwareBridgeDomain> vlanAwareBridgeDomains) {
    l2Interfaces.forEach(
        (name, l2Interface) -> {
          Optional<Layer2Settings> maybeL2 = interfaceTopologies.get(name).getLayer2Settings();
          assert maybeL2.isPresent();
          Layer2Settings l2 = maybeL2.get();

          // connect to L1 interface
          L1Interface l1Interface = l1Interfaces.get(l2.getL1Interface());
          assert l1Interface != null;
          l2Interface.connectToL1Interface(l1Interface, l2.getToL1());
          l1Interface.connectToL2Interface(l2Interface, l2.getFromL1());

          Layer2BridgeSettingsVisitor<Void> visitor =
              new Layer2BridgeSettingsVisitor<Void>() {
                @Override
                public Void visitLayer2VlanAwareBridgeSettings(
                    Layer2VlanAwareBridgeSettings layer2VlanAwareBridgeSettings) {
                  VlanAwareBridgeDomain vlanAwareBridgeDomain =
                      vlanAwareBridgeDomains.get(
                          layer2VlanAwareBridgeSettings.getVlanAwareBridgeDomain());
                  assert vlanAwareBridgeDomain != null;
                  l2Interface.connectToVlanAwareBridgeDomain(
                      vlanAwareBridgeDomain, layer2VlanAwareBridgeSettings.getToBridgeDomain());
                  vlanAwareBridgeDomain.connectToL2Interface(
                      l2Interface, layer2VlanAwareBridgeSettings.getFromBridgeDomain());
                  return null;
                }

                @Override
                public Void visitLayer2NonVlanAwareBridgeSettings(
                    Layer2NonVlanAwareBridgeSettings layer2NonVlanAwareBridgeSettings) {
                  NonVlanAwareBridgeDomain nonVlanAwareBridgeDomain =
                      nonVlanAwareBridgeDomains.get(
                          layer2NonVlanAwareBridgeSettings.getNonVlanAwareBridgeDomain());
                  assert nonVlanAwareBridgeDomain != null;
                  l2Interface.connectToNonVlanAwareBridgeDomain(
                      nonVlanAwareBridgeDomain,
                      L2ToNonVlanAwareBridgeDomain.of(
                          layer2NonVlanAwareBridgeSettings.getToBridgeDomain()));
                  nonVlanAwareBridgeDomain.connectToL2Interface(
                      l2Interface,
                      NonVlanAwareBridgeDomainToL2.of(
                          layer2NonVlanAwareBridgeSettings.getFromBridgeDomain()));
                  return null;
                }
              };
          for (Layer2BridgeSettings bridgeSettings : l2.getBridgeSettings()) {
            visitor.visit(bridgeSettings);
          }
        });
  }

  /**
   * Populate edges between {@link L3Interface}s and {@link L1Interface}s; and {@link L3Interface}s
   * and {@link BridgeDomain}s.
   */
  private static void computeL3InterfaceConnections(
      Map<String, InterfaceTopology> interfaceTopologies,
      Map<String, L3Interface> l3Interfaces,
      Map<String, L1Interface> l1Interfaces,
      Map<String, NonVlanAwareBridgeDomain> nonVlanAwareBridgeDomains,
      Map<String, VlanAwareBridgeDomain> vlanAwareBridgeDomains) {
    l3Interfaces.forEach(
        (name, l3Interface) -> {
          InterfaceTopology t = interfaceTopologies.get(name);
          assert t != null;
          Optional<Layer3Settings> maybeL3 = t.getLayer3Settings();
          assert maybeL3.isPresent();
          Layer3Settings l3 = maybeL3.get();
          new Layer3SettingsVisitor<Void>() {
            @Override
            public Void visitLayer3NonBridgedSettings(
                Layer3NonBridgedSettings layer3NonBridgedSettings) {
              assert l3Interface instanceof L3NonBridgedInterface;
              L3NonBridgedInterface l3NonBridged = (L3NonBridgedInterface) l3Interface;
              L1Interface l1 = l1Interfaces.get(layer3NonBridgedSettings.getL1Interface());
              assert l1 != null
                  : String.format(
                      "Missing l1 interface '%s' of l3 interface '%s'",
                      layer3NonBridgedSettings.getL1Interface(), l3Interface);
              l3NonBridged.connectToL1Interface(l1, layer3NonBridgedSettings.getToL1());
              l1.connectToL3NonBridgedInterface(l3NonBridged, layer3NonBridgedSettings.getFromL1());
              return null;
            }

            @Override
            public Void visitLayer3NonVlanAwareBridgeSettings(
                Layer3NonVlanAwareBridgeSettings layer3NonVlanAwareBridgeSettings) {
              assert l3Interface instanceof L3BridgedInterface;
              L3BridgedInterface l3Bridged = (L3BridgedInterface) l3Interface;
              NonVlanAwareBridgeDomain bridge =
                  nonVlanAwareBridgeDomains.get(
                      layer3NonVlanAwareBridgeSettings.getNonVlanAwareBridge());
              assert bridge != null;
              l3Bridged.connectToNonVlanAwareBridgeDomain(
                  bridge, layer3NonVlanAwareBridgeSettings.getToBridgeDomain());
              bridge.connectToL3Interface(
                  l3Bridged, layer3NonVlanAwareBridgeSettings.getFromBridgeDomain());
              return null;
            }

            @Override
            public Void visitLayer3VlanAwareBridgeSettings(
                Layer3VlanAwareBridgeSettings layer3VlanAwareBridgeSettings) {
              assert l3Interface instanceof L3BridgedInterface;
              L3BridgedInterface l3Bridged = (L3BridgedInterface) l3Interface;
              VlanAwareBridgeDomain bridge =
                  vlanAwareBridgeDomains.get(layer3VlanAwareBridgeSettings.getVlanAwareBridge());
              assert bridge != null;
              l3Bridged.connectToVlanAwareBridgeDomain(
                  bridge, layer3VlanAwareBridgeSettings.getToBridgeDomain());
              bridge.connectToL3Interface(
                  l3Bridged, layer3VlanAwareBridgeSettings.getFromBridgeDomain());
              return null;
            }

            @Override
            public Void visitLayer3TunnelSettings(Layer3TunnelSettings layer3TunnelSettings) {
              // nothing to do
              return null;
            }
          }.visit(l3);
        });
  }

  /** Populate edges between {@link L2Vni}s and {@link BridgeDomain}s. */
  private static void computeL2VniConnections(
      Map<Integer, Layer2Vni> configLayer2Vnis,
      Map<Integer, L2Vni> l2Vnis,
      Map<String, NonVlanAwareBridgeDomain> nonVlanAwareBridgeDomains,
      Map<String, VlanAwareBridgeDomain> vlanAwareBridgeDomains) {
    l2Vnis.forEach(
        (vni, l2vni) -> {
          Layer2Vni configLayer2Vni = configLayer2Vnis.get(vni);
          assert configLayer2Vni != null;
          if (configLayer2Vni.getVlan().isPresent()) {
            Optional<String> maybeBridgeName = configLayer2Vni.getVlanAwareBridgeDomain();
            assert maybeBridgeName.isPresent();
            String bridgeName = maybeBridgeName.get();
            VlanAwareBridgeDomain vlanAwareBridgeDomain = vlanAwareBridgeDomains.get(bridgeName);
            assert vlanAwareBridgeDomain != null;
            int vlan = configLayer2Vni.getVlan().get();
            l2vni.connectToVlanAwareBridgeDomain(
                vlanAwareBridgeDomain, l2VniToVlanAwareBridgeDomain(vlan));
            vlanAwareBridgeDomain.connectToL2Vni(l2vni, vlanAwareBridgeDomainToL2Vni(vlan));
          } else {
            assert configLayer2Vni.getNonVlanAwareBridgeDomain().isPresent();
            String bridgeName = configLayer2Vni.getNonVlanAwareBridgeDomain().get();
            NonVlanAwareBridgeDomain bridge = nonVlanAwareBridgeDomains.get(bridgeName);
            assert bridge != null;
            l2vni.connectToNonVlanAwareBridgeDomain(bridge);
            bridge.connectToL2Vni(l2vni, instance());
          }
        });
  }

  private DeviceTopologyUtils() {}
}
