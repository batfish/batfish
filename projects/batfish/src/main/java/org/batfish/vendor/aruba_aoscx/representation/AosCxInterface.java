package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;

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

  public @Nullable Double getBandwidth() {
    return _bandwidth;
  }

  public void setBandwidth(double bandwidth) {
    _bandwidth = bandwidth;
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

  public @Nullable ConcreteInterfaceAddress getAddress() {
    return _address;
  }

  public void setAddress(ConcreteInterfaceAddress address) {
    _address = address;
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
  private @Nullable ConcreteInterfaceAddress _address;
  private @Nullable Double _bandwidth;
  private @Nullable Integer _ospfProcessId;
  private @Nullable String _ospfArea;
  private @Nullable Integer _ospfCost;
  private @Nullable OspfNetworkType _ospfNetworkType;
  private @Nullable String _vrfName;
  private @Nullable String _incomingAcl;
  private @Nullable String _outgoingAcl;
}
