package org.batfish.representation.cisco_nxos;

import com.google.common.collect.Range;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.SwitchportMode;

/** A layer-2- or layer-3-capable network interface */
public final class Interface implements Serializable {

  public static final IntegerSpace VLAN_RANGE = IntegerSpace.of(Range.closed(1, 4094));

  private static boolean defaultShutdown(
      SwitchportMode switchportMode, CiscoNxosInterfaceType type) {
    switch (type) {
      case ETHERNET:
      case PORT_CHANNEL:
        return switchportMode == SwitchportMode.NONE;

      case LOOPBACK:
      case MGMT:
      case VLAN:
      default:
        return false;
    }
  }

  private @Nullable Integer _accessVlan;
  private @Nullable InterfaceAddress _address;
  private @Nullable IntegerSpace _allowedVlans;
  private final @Nonnull Set<String> _declaredNames;
  private @Nullable Integer _encapsulationVlan;
  private final @Nonnull String _name;
  private @Nullable Integer _nativeVlan;
  private final @Nullable String _parentInterface;
  private final @Nonnull Set<InterfaceAddress> _secondaryAddresses;
  private @Nullable Boolean _shutdown;
  private @Nonnull SwitchportMode _switchportMode;
  private final @Nonnull CiscoNxosInterfaceType _type;
  private @Nullable String _vrfMember;

  public Interface(String name, String parentInterface, CiscoNxosInterfaceType type) {
    _name = name;
    _parentInterface = parentInterface;
    _declaredNames = new HashSet<>();
    _secondaryAddresses = new HashSet<>();
    _type = type;
    initDefaultSwitchportSettings(parentInterface != null, type);

    // Set defaults for individual switchport modes
    // - only effective when correspoinding switchport mode is active
    _accessVlan = 1;
    _nativeVlan = 1;
    _allowedVlans = VLAN_RANGE;
  }

  public @Nullable Integer getAccessVlan() {
    return _accessVlan;
  }

  /** The primary IPv4 address of the interface. */
  public @Nullable InterfaceAddress getAddress() {
    return _address;
  }

  public @Nullable IntegerSpace getAllowedVlans() {
    return _allowedVlans;
  }

  public @Nonnull Set<String> getDeclaredNames() {
    return _declaredNames;
  }

  public @Nullable Integer getEncapsulationVlan() {
    return _encapsulationVlan;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Integer getNativeVlan() {
    return _nativeVlan;
  }

  public @Nullable String getParentInterface() {
    return _parentInterface;
  }

  /** The set of secondary IPv4 addresses of the interface. */
  public @Nonnull Set<InterfaceAddress> getSecondaryAddresses() {
    return _secondaryAddresses;
  }

  public boolean getShutdown() {
    return _shutdown != null ? _shutdown : defaultShutdown(_switchportMode, _type);
  }

  public SwitchportMode getSwitchportMode() {
    return _switchportMode;
  }

  public @Nullable String getVrfMember() {
    return _vrfMember;
  }

  private void initDefaultSwitchportSettings(boolean isSubinterface, CiscoNxosInterfaceType type) {
    switch (type) {
      case ETHERNET:
      case PORT_CHANNEL:
        if (isSubinterface) {
          // this is a subinterface
          _switchportMode = SwitchportMode.NONE;
        } else {
          // this is a parent interface
          _switchportMode = SwitchportMode.ACCESS;
        }
        break;

      case LOOPBACK:
      case MGMT:
      default:
        _switchportMode = SwitchportMode.NONE;
        break;
    }
  }

  public void setAccessVlan(@Nullable Integer accessVlan) {
    _accessVlan = accessVlan;
  }

  public void setAddress(@Nullable InterfaceAddress address) {
    _address = address;
  }

  public void setAllowedVlans(@Nullable IntegerSpace allowedVlans) {
    _allowedVlans = allowedVlans;
  }

  public void setEncapsulationVlan(@Nullable Integer encapsulationVlan) {
    _encapsulationVlan = encapsulationVlan;
  }

  public void setNativeVlan(@Nullable Integer nativeVlan) {
    _nativeVlan = nativeVlan;
  }

  public void setShutdown(@Nullable Boolean shutdown) {
    _shutdown = shutdown;
  }

  public void setSwitchportMode(SwitchportMode switchportMode) {
    _switchportMode = switchportMode;
  }
}
