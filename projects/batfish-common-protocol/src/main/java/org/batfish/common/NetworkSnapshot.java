package org.batfish.common;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;

/** A {@link NetworkSnapshot} represents a {@link Snapshot} in a specific network. */
public class NetworkSnapshot {
  /**
   * Returns a new {@link NetworkSnapshot} corresponding to the given {@code network} and {@code
   * snapshot}.
   */
  public NetworkSnapshot(@Nonnull String network, @Nonnull Snapshot snapshot) {
    _network = network;
    _snapshot = snapshot;
  }

  public String getNetwork() {
    return _network;
  }

  public Snapshot getSnapshot() {
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

  private final String _network;
  private final Snapshot _snapshot;
}
