package org.batfish.topology;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.ExecutionException;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.topology.IpOwners;
import org.batfish.common.topology.TopologyProvider;

/** */
@ParametersAreNonnullByDefault
public final class TopologyProviderImpl implements TopologyProvider {
  private final IBatfish _batfish;
  private final Cache<NetworkSnapshot, IpOwners> _ipOwners;

  /** Create a new topology provider for a given instance of {@link IBatfish} */
  public TopologyProviderImpl(IBatfish batfish) {
    _batfish = batfish;
    _ipOwners = CacheBuilder.newBuilder().maximumSize(6).build();
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
}
