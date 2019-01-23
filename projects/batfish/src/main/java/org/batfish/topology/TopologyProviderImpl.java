package org.batfish.topology;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.topology.IpOwners;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.topology.Layer2Topology;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.common.topology.TopologyUtil;
import org.batfish.common.util.IpsecUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.vxlan.VxlanTopology;

/** */
@ParametersAreNonnullByDefault
public final class TopologyProviderImpl implements TopologyProvider {
  private static final int MAX_CACHED_SNAPSHOTS = 3;

  private final IBatfish _batfish;
  private final Cache<NetworkSnapshot, IpOwners> _ipOwners;
  private final Cache<NetworkSnapshot, Layer1Topology> _layer1LogicalTopologies;
  private final Cache<NetworkSnapshot, Layer1Topology> _layer1PhysicalTopologies;
  private final Cache<NetworkSnapshot, Layer2Topology> _layer2Topologies;
  private final Cache<NetworkSnapshot, Topology> _layer3Topologies;
  private final Cache<NetworkSnapshot, Optional<Layer1Topology>> _rawLayer1PhysicalTopologies;
  private final Cache<NetworkSnapshot, Topology> _rawLayer3Topologies;
  private final Cache<NetworkSnapshot, VxlanTopology> _vxlanTopologies;

  /** Create a new topology provider for a given instance of {@link IBatfish} */
  public TopologyProviderImpl(IBatfish batfish) {
    _batfish = batfish;
    _ipOwners = CacheBuilder.newBuilder().maximumSize(MAX_CACHED_SNAPSHOTS).build();
    _layer1LogicalTopologies = CacheBuilder.newBuilder().maximumSize(MAX_CACHED_SNAPSHOTS).build();
    _layer1PhysicalTopologies = CacheBuilder.newBuilder().maximumSize(MAX_CACHED_SNAPSHOTS).build();
    _layer2Topologies =
        CacheBuilder.newBuilder().maximumSize(MAX_CACHED_SNAPSHOTS).softValues().build();
    _layer3Topologies = CacheBuilder.newBuilder().maximumSize(MAX_CACHED_SNAPSHOTS).build();
    _rawLayer1PhysicalTopologies =
        CacheBuilder.newBuilder().maximumSize(MAX_CACHED_SNAPSHOTS).build();
    _rawLayer3Topologies = CacheBuilder.newBuilder().maximumSize(MAX_CACHED_SNAPSHOTS).build();
    _vxlanTopologies = CacheBuilder.newBuilder().maximumSize(MAX_CACHED_SNAPSHOTS).build();
  }

  private Topology computeLayer3Topology(NetworkSnapshot networkSnapshot) {
    Map<String, Configuration> configurations = _batfish.loadConfigurations(networkSnapshot);
    Topology topology = getRawLayer3Topology(networkSnapshot);
    topology.prune(
        _batfish.getEdgeBlacklist(networkSnapshot),
        _batfish.getNodeBlacklist(networkSnapshot),
        _batfish.getInterfaceBlacklist(networkSnapshot));
    topology.pruneFailedIpsecSessionEdges(
        IpsecUtil.initIpsecTopology(configurations), configurations);
    return topology;
  }

  private Topology computeRawLayer3Topology(NetworkSnapshot networkSnapshot) {
    Map<String, Configuration> configurations = _batfish.loadConfigurations(networkSnapshot);
    if (!getRawLayer1PhysicalTopology(networkSnapshot).isPresent()) {
      return TopologyUtil.synthesizeL3Topology(configurations);
    } else {
      return TopologyUtil.computeLayer3Topology(getLayer2Topology(networkSnapshot), configurations);
    }
  }

  /** Return the {@link IpOwners} for a given snapshot. */
  @Override
  public IpOwners getIpOwners(NetworkSnapshot snapshot) {
    try {
      return _ipOwners.get(snapshot, () -> new IpOwners(_batfish.loadConfigurations(snapshot)));
    } catch (ExecutionException e) {
      return new IpOwners(_batfish.loadConfigurations(snapshot));
    }
  }

  @Override
  public Layer1Topology getLayer1LogicalTopology(NetworkSnapshot networkSnapshot) {
    try {
      return _layer1LogicalTopologies.get(
          networkSnapshot,
          () ->
              TopologyUtil.computeLayer1LogicalTopology(
                  getLayer1PhysicalTopology(networkSnapshot),
                  _batfish.loadConfigurations(networkSnapshot)));
    } catch (ExecutionException e) {
      return TopologyUtil.computeLayer1LogicalTopology(
          getLayer1PhysicalTopology(networkSnapshot), _batfish.loadConfigurations(networkSnapshot));
    }
  }

  @Override
  public Layer1Topology getLayer1PhysicalTopology(NetworkSnapshot networkSnapshot) {
    try {
      return _layer1PhysicalTopologies.get(
          networkSnapshot,
          () ->
              TopologyUtil.computeLayer1PhysicalTopology(
                  getRawLayer1PhysicalTopology(networkSnapshot).get(),
                  _batfish.loadConfigurations(networkSnapshot)));
    } catch (ExecutionException e) {
      return TopologyUtil.computeLayer1PhysicalTopology(
          getRawLayer1PhysicalTopology(networkSnapshot).get(),
          _batfish.loadConfigurations(networkSnapshot));
    }
  }

  @Override
  public Layer2Topology getLayer2Topology(NetworkSnapshot networkSnapshot) {
    try {
      return _layer2Topologies.get(
          networkSnapshot,
          () ->
              TopologyUtil.computeLayer2Topology(
                  getLayer1LogicalTopology(networkSnapshot),
                  _batfish.loadConfigurations(networkSnapshot)));
    } catch (ExecutionException e) {
      return TopologyUtil.computeLayer2Topology(
          getLayer1LogicalTopology(networkSnapshot), _batfish.loadConfigurations(networkSnapshot));
    }
  }

  @Override
  public Topology getLayer3Topology(NetworkSnapshot networkSnapshot) {
    try {
      return _layer3Topologies.get(networkSnapshot, () -> computeLayer3Topology(networkSnapshot));
    } catch (ExecutionException e) {
      return computeLayer3Topology(networkSnapshot);
    }
  }

  @Override
  public Optional<Layer1Topology> getRawLayer1PhysicalTopology(NetworkSnapshot networkSnapshot) {
    try {
      return _rawLayer1PhysicalTopologies.get(
          networkSnapshot,
          () -> Optional.ofNullable(_batfish.loadRawLayer1PhysicalTopology(networkSnapshot)));
    } catch (ExecutionException e) {
      return Optional.ofNullable(_batfish.loadRawLayer1PhysicalTopology(networkSnapshot));
    }
  }

  @Override
  public Topology getRawLayer3Topology(NetworkSnapshot networkSnapshot) {
    try {
      return _rawLayer3Topologies.get(
          networkSnapshot, () -> computeRawLayer3Topology(networkSnapshot));
    } catch (ExecutionException e) {
      return computeRawLayer3Topology(networkSnapshot);
    }
  }

  @Override
  public VxlanTopology getVxlanTopology(NetworkSnapshot snapshot) {
    try {
      return _vxlanTopologies.get(
          snapshot, () -> new VxlanTopology(_batfish.loadConfigurations(snapshot)));
    } catch (ExecutionException e) {
      return new VxlanTopology(_batfish.loadConfigurations(snapshot));
    }
  }
}
