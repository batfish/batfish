package org.batfish.datamodel.ospf;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Uniquely identifies an OSPF configuration ({@link OspfNeighbor}) in the network. */
@ParametersAreNonnullByDefault
public final class OspfNeighborConfigId implements Serializable {
  private static final long serialVersionUID = 1;

  private final String _hostname;
  private final String _vrfName;
  private final String _interfaceName;

  public OspfNeighborConfigId(String hostname, String vrfName, String interfaceName) {
    _hostname = hostname;
    _vrfName = vrfName;
    _interfaceName = interfaceName;
  }

  public String getHostname() {
    return _hostname;
  }

  public String getVrfName() {
    return _vrfName;
  }

  public String getInterfaceName() {
    return _interfaceName;
  }

  public NodeInterfacePair getNodeInterfacePair() {
    return new NodeInterfacePair(getHostname(), getInterfaceName());
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OspfNeighborConfigId)) {
      return false;
    }
    OspfNeighborConfigId other = (OspfNeighborConfigId) o;
    return Objects.equals(_hostname, other._hostname)
        && Objects.equals(_vrfName, other._vrfName)
        && Objects.equals(_interfaceName, other._interfaceName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _vrfName, _interfaceName);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hostname", _hostname)
        .add("vrfName", _vrfName)
        .add("intefaceName", _interfaceName)
        .toString();
  }
}
