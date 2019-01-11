package org.batfish.datamodel.questions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** Contains the values needed to populate a table answer row with VXLAN VNI properties. */
@ParametersAreNonnullByDefault
public final class VxlanVniPropertiesRow {

  private final String _node;
  private final Integer _vni;
  private final Integer _vlan;
  private final Ip _localVtepIp;
  private final Ip _multicastGroup;
  private final Iterable<Ip> _vtepFloodList;
  private final Integer _vxlanPort;

  public VxlanVniPropertiesRow(
      String node,
      Integer vni,
      @Nullable Integer vlan,
      @Nullable Ip localVtepIp,
      @Nullable Ip multicastGroup,
      @Nullable Iterable<Ip> vtepFloodList,
      Integer vxlanPort) {
    _node = node;
    _vni = vni;
    _vlan = vlan;
    _localVtepIp = localVtepIp;
    _multicastGroup = multicastGroup;
    _vtepFloodList = vtepFloodList;
    _vxlanPort = vxlanPort;
  }

  public @Nonnull String getNode() {
    return _node;
  }

  public @Nonnull Integer getVni() {
    return _vni;
  }

  public @Nullable Integer getVlan() {
    return _vlan;
  }

  public @Nullable Ip getLocalVtepIp() {
    return _localVtepIp;
  }

  public @Nullable Ip getMulticastGroup() {
    return _multicastGroup;
  }

  public @Nullable Iterable<Ip> getVtepFloodList() {
    return _vtepFloodList;
  }

  public @Nonnull Integer getVxlanPort() {
    return _vxlanPort;
  }
}
