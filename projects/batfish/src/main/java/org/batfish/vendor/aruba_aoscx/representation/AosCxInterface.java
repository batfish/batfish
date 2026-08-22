package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.ConcreteInterfaceAddress6;
import org.batfish.datamodel.IntegerSpace;

/** Vendor-specific representation of an Aruba AOS-CX interface. */
public final class AosCxInterface implements Serializable {

  public enum OspfNetworkType {
    BROADCAST,
    POINT_TO_POINT
  }

  public AosCxInterface(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Boolean getEnabled() {
    return _enabled;
  }

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public @Nullable Double getBandwidth() {
    return _bandwidth;
  }

  public void setBandwidth(double bandwidth) {
    _bandwidth = bandwidth;
  }

  public @Nullable Integer getMtu() {
    return _mtu;
  }

  public void setMtu(int mtu) {
    _mtu = mtu;
  }

  public @Nullable Integer getIpMtu() {
    return _ipMtu;
  }

  public void setIpMtu(int ipMtu) {
    _ipMtu = ipMtu;
  }

  public @Nullable Integer getOspfProcessId() {
    return _ospfProcessId;
  }

  public void setOspfProcessId(int ospfProcessId) {
    _ospfProcessId = ospfProcessId;
  }

  public @Nullable String getOspfArea() {
    return _ospfArea;
  }

  public void setOspfArea(String ospfArea) {
    _ospfArea = ospfArea;
  }

  public @Nullable Integer getOspfCost() {
    return _ospfCost;
  }

  public void setOspfCost(int ospfCost) {
    _ospfCost = ospfCost;
  }

  public @Nullable OspfNetworkType getOspfNetworkType() {
    return _ospfNetworkType;
  }

  public void setOspfNetworkType(OspfNetworkType ospfNetworkType) {
    _ospfNetworkType = ospfNetworkType;
  }

  public @Nullable Integer getOspfv3ProcessId() {
    return _ospfv3ProcessId;
  }

  public void setOspfv3ProcessId(int processId) {
    _ospfv3ProcessId = processId;
  }

  public @Nullable String getOspfv3Area() {
    return _ospfv3Area;
  }

  public void setOspfv3Area(String area) {
    _ospfv3Area = area;
  }

  public @Nullable Integer getOspfv3Cost() {
    return _ospfv3Cost;
  }

  public void setOspfv3Cost(int cost) {
    _ospfv3Cost = cost;
  }

  public @Nullable OspfNetworkType getOspfv3NetworkType() {
    return _ospfv3NetworkType;
  }

  public void setOspfv3NetworkType(OspfNetworkType networkType) {
    _ospfv3NetworkType = networkType;
  }

  public @Nullable ConcreteInterfaceAddress getAddress() {
    return _address;
  }

  public void setAddress(ConcreteInterfaceAddress address) {
    _address = address;
  }

  public @Nonnull List<ConcreteInterfaceAddress6> getIpv6Addresses() {
    return _ipv6Addresses;
  }

  public void addIpv6Address(ConcreteInterfaceAddress6 address) {
    _ipv6Addresses.add(address);
  }

  public boolean getIpv6LinkLocalEnabled() {
    return _ipv6LinkLocalEnabled;
  }

  public void setIpv6LinkLocalEnabled(boolean enabled) {
    _ipv6LinkLocalEnabled = enabled;
  }

  public @Nullable Boolean getSwitchport() {
    return _switchport;
  }

  public void setSwitchport(boolean switchport) {
    _switchport = switchport;
  }

  public @Nullable Integer getNativeVlan() {
    return _nativeVlan;
  }

  public void setNativeVlan(int nativeVlan) {
    _nativeVlan = nativeVlan;
  }

  public boolean getNativeVlanTagged() {
    return _nativeVlanTagged;
  }

  public void setNativeVlanTagged(boolean nativeVlanTagged) {
    _nativeVlanTagged = nativeVlanTagged;
  }

  public @Nullable IntegerSpace getAllowedVlans() {
    return _allowedVlans;
  }

  public void setAllowedVlans(IntegerSpace allowedVlans) {
    _allowedVlans = allowedVlans;
  }

  public @Nullable String getLagName() {
    return _lagName;
  }

  public void setLagName(String lagName) {
    _lagName = lagName;
  }

  public @Nullable String getVrfName() {
    return _vrfName;
  }

  public void setVrfName(String vrfName) {
    _vrfName = vrfName;
  }

  public @Nullable String getIncomingAcl() {
    return _incomingAcl;
  }

  public void setIncomingAcl(String incomingAcl) {
    _incomingAcl = incomingAcl;
  }

  public @Nullable String getOutgoingAcl() {
    return _outgoingAcl;
  }

  public void setOutgoingAcl(String outgoingAcl) {
    _outgoingAcl = outgoingAcl;
  }

  private final @Nonnull String _name;
  private @Nullable Boolean _enabled;
  private @Nullable String _description;
  private @Nullable ConcreteInterfaceAddress _address;
  private final @Nonnull List<ConcreteInterfaceAddress6> _ipv6Addresses =
      new ArrayList<>();
  private boolean _ipv6LinkLocalEnabled;
  private @Nullable Double _bandwidth;
  private @Nullable Integer _mtu;
  private @Nullable Integer _ipMtu;
  private @Nullable Integer _ospfProcessId;
  private @Nullable String _ospfArea;
  private @Nullable Integer _ospfCost;
  private @Nullable OspfNetworkType _ospfNetworkType;
  private @Nullable Integer _ospfv3ProcessId;
  private @Nullable String _ospfv3Area;
  private @Nullable Integer _ospfv3Cost;
  private @Nullable OspfNetworkType _ospfv3NetworkType;
  private @Nullable Boolean _switchport;
  private @Nullable Integer _nativeVlan;
  private boolean _nativeVlanTagged;
  private @Nullable IntegerSpace _allowedVlans;
  private @Nullable String _lagName;
  private @Nullable String _vrfName;
  private @Nullable String _incomingAcl;
  private @Nullable String _outgoingAcl;
}
