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

  /**
   * Explicit interface-level OSPFv3 IPsec authentication.
   *
   * <p>Null means either inherit area authentication or use the explicit
   * authentication-null override; consult {@link #getOspfv3AuthenticationNull()}.
   */
  public @Nullable AosCxOspfv3Authentication
      getOspfv3Authentication() {

    return _ospfv3Authentication;
  }

  public boolean getOspfv3AuthenticationNull() {
    return _ospfv3AuthenticationNull;
  }

  public void setOspfv3Authentication(
      AosCxOspfv3Authentication authentication) {

    _ospfv3Authentication =
        authentication;

    _ospfv3AuthenticationNull =
        false;
  }

  public void setOspfv3AuthenticationNull() {
    _ospfv3Authentication =
        null;

    _ospfv3AuthenticationNull =
        true;
  }

  /**
   * Remove the interface-level authentication override and restore area
   * inheritance.
   */
  public void clearOspfv3Authentication() {
    _ospfv3Authentication =
        null;

    _ospfv3AuthenticationNull =
        false;
  }

  /**
   * Explicit interface-level OSPFv3 IPsec ESP configuration.
   *
   * <p>Null means either inherit area ESP configuration or use the explicit
   * encryption-null override; consult {@link #getOspfv3EncryptionNull()}.
   */
  public @Nullable AosCxOspfv3Encryption
      getOspfv3Encryption() {

    return _ospfv3Encryption;
  }

  public boolean getOspfv3EncryptionNull() {
    return _ospfv3EncryptionNull;
  }

  public void setOspfv3Encryption(
      AosCxOspfv3Encryption encryption) {

    _ospfv3Encryption =
        encryption;

    _ospfv3EncryptionNull =
        false;
  }

  /**
   * Configure interface-level NULL ESP, which suppresses inherited ESP.
   */
  public void setOspfv3EncryptionNull() {

    _ospfv3Encryption =
        null;

    _ospfv3EncryptionNull =
        true;
  }

  /**
   * Remove the interface ESP override and restore area inheritance.
   */
  public void clearOspfv3Encryption() {

    _ospfv3Encryption =
        null;

    _ospfv3EncryptionNull =
        false;
  }

  /**
   * Explicit OSPFv3 BFD interface override.
   *
   * <p>Null means inherit the process-wide bfd all-interfaces setting.
   * True means explicitly enabled. False means explicitly disabled.
   */
  public @Nullable Boolean getOspfv3Bfd() {
    return _ospfv3Bfd;
  }

  public void setOspfv3Bfd(
      boolean enabled) {

    _ospfv3Bfd =
        enabled;
  }

  public void clearOspfv3Bfd() {
    _ospfv3Bfd =
        null;
  }

  public @Nullable Integer getOspfv3Cost() {
    return _ospfv3Cost;
  }

  public void setOspfv3Cost(int cost) {
    _ospfv3Cost = cost;
  }

  public void clearOspfv3Cost() {
    _ospfv3Cost = null;
  }

  public @Nullable Integer getOspfv3HelloInterval() {
    return _ospfv3HelloInterval;
  }

  public void setOspfv3HelloInterval(int interval) {
    _ospfv3HelloInterval = interval;
  }

  public void clearOspfv3HelloInterval() {
    _ospfv3HelloInterval = null;
  }

  public @Nullable Integer getOspfv3DeadInterval() {
    return _ospfv3DeadInterval;
  }

  public void setOspfv3DeadInterval(int interval) {
    _ospfv3DeadInterval = interval;
  }

  public void clearOspfv3DeadInterval() {
    _ospfv3DeadInterval = null;
  }

  public @Nullable OspfNetworkType getOspfv3NetworkType() {
    return _ospfv3NetworkType;
  }

  public void setOspfv3NetworkType(OspfNetworkType networkType) {
    _ospfv3NetworkType = networkType;
  }

  public void clearOspfv3NetworkType() {
    _ospfv3NetworkType = null;
  }

  public @Nullable Integer getOspfv3Priority() {
    return _ospfv3Priority;
  }

  public void setOspfv3Priority(int priority) {
    _ospfv3Priority = priority;
  }

  public void clearOspfv3Priority() {
    _ospfv3Priority = null;
  }

  public @Nullable Integer getOspfv3RetransmitInterval() {
    return _ospfv3RetransmitInterval;
  }

  public void setOspfv3RetransmitInterval(int interval) {
    _ospfv3RetransmitInterval = interval;
  }

  public void clearOspfv3RetransmitInterval() {
    _ospfv3RetransmitInterval = null;
  }

  public @Nullable Integer getOspfv3TransitDelay() {
    return _ospfv3TransitDelay;
  }

  public void setOspfv3TransitDelay(int delay) {
    _ospfv3TransitDelay = delay;
  }

  public void clearOspfv3TransitDelay() {
    _ospfv3TransitDelay = null;
  }

  /**
   * Explicit interface-level OSPFv3 passive setting.
   *
   * <p>Null means inherit the process passive-interface default.
   */
  public @Nullable Boolean getOspfv3Passive() {
    return _ospfv3Passive;
  }

  public void setOspfv3Passive(boolean passive) {
    _ospfv3Passive = passive;
  }

  /**
   * Explicit protocol-level enable state for OSPFv3 on this interface.
   *
   * <p>Null means the AOS-CX default: OSPFv3 is enabled when the
   * interface is attached to an OSPFv3 area.
   */
  public @Nullable Boolean getOspfv3Enabled() {
    return _ospfv3Enabled;
  }

  public void setOspfv3Enabled(boolean enabled) {
    _ospfv3Enabled = enabled;
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

  public @Nullable String getIncomingIpv6Acl() {
    return _incomingIpv6Acl;
  }

  public void setIncomingIpv6Acl(
      String incomingIpv6Acl) {
    _incomingIpv6Acl = incomingIpv6Acl;
  }

  public @Nullable String getOutgoingIpv6Acl() {
    return _outgoingIpv6Acl;
  }

  public void setOutgoingIpv6Acl(
      String outgoingIpv6Acl) {
    _outgoingIpv6Acl = outgoingIpv6Acl;
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
  private @Nullable AosCxOspfv3Authentication
      _ospfv3Authentication;
  private boolean _ospfv3AuthenticationNull;
  private @Nullable AosCxOspfv3Encryption
      _ospfv3Encryption;
  private boolean _ospfv3EncryptionNull;
  private @Nullable Boolean _ospfv3Bfd;
  private @Nullable Integer _ospfv3Cost;
  private @Nullable Integer _ospfv3HelloInterval;
  private @Nullable Integer _ospfv3DeadInterval;
  private @Nullable OspfNetworkType _ospfv3NetworkType;
  private @Nullable Integer _ospfv3Priority;
  private @Nullable Integer _ospfv3RetransmitInterval;
  private @Nullable Integer _ospfv3TransitDelay;
  private @Nullable Boolean _ospfv3Passive;
  private @Nullable Boolean _ospfv3Enabled;
  private @Nullable Boolean _switchport;
  private @Nullable Integer _nativeVlan;
  private boolean _nativeVlanTagged;
  private @Nullable IntegerSpace _allowedVlans;
  private @Nullable String _lagName;
  private @Nullable String _vrfName;
  private @Nullable String _incomingAcl;
  private @Nullable String _outgoingAcl;
  private @Nullable String _incomingIpv6Acl;
  private @Nullable String _outgoingIpv6Acl;
}
