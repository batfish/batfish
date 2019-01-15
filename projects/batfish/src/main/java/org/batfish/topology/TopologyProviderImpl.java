package org.batfish.topology;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.ExecutionException;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.topology.IpOwners;
import org.batfish.common.topology.TopologyProvider;
import org.batfish.datamodel.vxlan.VxlanTopology;

/** */
@ParametersAreNonnullByDefault
public final class TopologyProviderImpl implements TopologyProvider {
  private static final int MAX_CACHED_SNAPSHOTS = 3;

  private final IBatfish _batfish;
  private final Cache<NetworkSnapshot, IpOwners> _ipOwners;
  private final Cache<NetworkSnapshot, VxlanTopology> _vxlanTopologies;

  /** Create a new topology provider for a given instance of {@link IBatfish} */
  public TopologyProviderImpl(IBatfish batfish) {
    _batfish = batfish;
    _ipOwners = CacheBuilder.newBuilder().maximumSize(MAX_CACHED_SNAPSHOTS).build();
    _vxlanTopologies = CacheBuilder.newBuilder().maximumSize(MAX_CACHED_SNAPSHOTS).build();
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
  public VxlanTopology getVxlanTopology(NetworkSnapshot snapshot) {
    try {
      return _vxlanTopologies.get(
          snapshot, () -> new VxlanTopology(_batfish.loadConfigurations(snapshot)));
    } catch (ExecutionException e) {
      return new VxlanTopology(_batfish.loadConfigurations(snapshot));
    }
  }
}
