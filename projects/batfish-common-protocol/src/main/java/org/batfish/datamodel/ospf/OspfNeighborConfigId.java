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
  private final String _procName;
  private final String _interfaceName;

  /**
   * Create a new unique identifier for an OSPF process
   *
   * @param hostname the hostname on which the neighbor exists
   * @param vrfName the name of a VRF on which the neighbor exists
   * @param procName the name of OSPF process on which the neighbor exists
   * @param interfaceName the interface name
   */
  public OspfNeighborConfigId(
      String hostname, String vrfName, String procName, String interfaceName) {
    _hostname = hostname;
    _vrfName = vrfName;
    _procName = procName;
    _interfaceName = interfaceName;
  }

  public String getHostname() {
    return _hostname;
  }

  public String getVrfName() {
    return _vrfName;
  }

  public String getProcName() {
    return _procName;
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
        && Objects.equals(_procName, other._procName)
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
        .add("procName", _procName)
        .add("intefaceName", _interfaceName)
        .toString();
  }
}
