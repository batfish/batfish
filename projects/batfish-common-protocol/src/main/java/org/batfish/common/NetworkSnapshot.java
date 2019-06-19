package org.batfish.common;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;

/**
 * A {@link NetworkSnapshot} represents a {@link SnapshotId snapshot} in a specific {@link NetworkId
 * network}.
 */
public class NetworkSnapshot {
  /**
   * Returns a new {@link NetworkSnapshot} corresponding to the given {@code network} and {@code
   * snapshot}.
   */
  public NetworkSnapshot(@Nonnull NetworkId network, @Nonnull SnapshotId snapshot) {
    _network = network;
    _snapshot = snapshot;
  }

  public NetworkId getNetwork() {
    return _network;
  }

  public SnapshotId getSnapshot() {
    return _snapshot;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof NetworkSnapshot)) {
      return false;
    }
    NetworkSnapshot other = (NetworkSnapshot) o;
    return _network.equals(other._network) && _snapshot.equals(other._snapshot);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_network, _snapshot);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(NetworkSnapshot.class)
        .add("network", _network)
        .add("snapshot", _snapshot)
        .toString();
  }

  private final NetworkId _network;
  private final SnapshotId _snapshot;
}
