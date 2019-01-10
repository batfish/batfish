package org.batfish.datamodel.questions;

import org.batfish.datamodel.Ip;

/** Contains the values needed to populate a table answer row with VXLAN VNI properties. */
public class VxlanVniPropertiesRow {

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
      Integer vlan,
      Ip localVtepIp,
      Ip multicastGroup,
      Iterable<Ip> vtepFloodList,
      Integer vxlanPort) {
    _node = node;
    _vni = vni;
    _vlan = vlan;
    _localVtepIp = localVtepIp;
    _multicastGroup = multicastGroup;
    _vtepFloodList = vtepFloodList;
    _vxlanPort = vxlanPort;
  }

  public String getNode() {
    return _node;
  }

  public Integer getVni() {
    return _vni;
  }

  public Integer getVlan() {
    return _vlan;
  }

  public Ip getLocalVtepIp() {
    return _localVtepIp;
  }

  public Ip getMulticastGroup() {
    return _multicastGroup;
  }

  public Iterable<Ip> getVtepFloodList() {
    return _vtepFloodList;
  }

  public Integer getVxlanPort() {
    return _vxlanPort;
  }
}
