package org.batfish.common.topology.bridge_domain.node;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.Objects;
import javax.annotation.Nonnull;

/** A {@link VlanAwareBridgeDomain} or a {@link NonVlanAwareBridgeDomain}. */
public interface BridgeDomain extends Node {

  final class Id {
    public static @Nonnull Id of(String hostname, String bridgeName) {
      return new Id(hostname, bridgeName);
    }

    public @Nonnull String getHostname() {
      return _hostname;
    }

    public @Nonnull String getBridgeName() {
      return _bridgeName;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof Id)) {
        return false;
      }
      Id id = (Id) o;
      return _hostname.equals(id._hostname) && _bridgeName.equals(id._bridgeName);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_hostname, _bridgeName);
    }

    @Override
    public String toString() {
      return toStringHelper(this)
          .add("_hostname", _hostname)
          .add("_bridgeName", _bridgeName)
          .toString();
    }

    private Id(String hostname, String bridgeName) {
      _hostname = hostname;
      _bridgeName = bridgeName;
    }

    private final @Nonnull String _hostname;
    private final @Nonnull String _bridgeName;
  }
}
