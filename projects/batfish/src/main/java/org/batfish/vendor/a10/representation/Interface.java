package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/** Datamodel class representing a configured A10 interface. */
public class Interface implements Serializable {
  public enum Type {
    ETHERNET,
    LOOPBACK,
    TRUNK,
    VE,
  }

  public static final int DEFAULT_MTU = 1500;

  public @Nullable String getAccessListIn() {
    return _accessListIn;
  }

  public @Nullable Boolean getEnabled() {
    return _enabled;
  }

  public @Nullable ConcreteInterfaceAddress getIpAddress() {
    return _ipAddress;
  }

  public @Nullable Integer getMtu() {
    return _mtu;
  }

  public @Nullable String getName() {
    return _name;
  }

  public int getNumber() {
    return _number;
  }

  public TrunkGroup getTrunkGroup() {
    return _trunkGroup;
  }

  public @Nonnull Type getType() {
    return _type;
  }

  public void setAccessListIn(@Nullable String accessListIn) {
    _accessListIn = accessListIn;
  }

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  public void setIpAddress(ConcreteInterfaceAddress ipAddress) {
    _ipAddress = ipAddress;
  }

  public void setMtu(Integer mtu) {
    _mtu = mtu;
  }

  public void setName(String name) {
    _name = name;
  }

  public void setTrunkGroup(TrunkGroup trunkGroup) {
    _trunkGroup = trunkGroup;
  }

  public @Nullable InterfaceLldp getLldp() {
    return _lldp;
  }

  /** Get LLDP configuration object for this interface, creating one if it doesn't already exist. */
  public @Nonnull InterfaceLldp getOrCreateLldp() {
    if (_lldp == null) {
      _lldp = new InterfaceLldp();
    }
    return _lldp;
  }

  public Interface(Type type, int number) {
    _number = number;
    _type = type;
  }

  private @Nullable String _accessListIn;
  private @Nullable Boolean _enabled;
  private final @Nonnull Type _type;
  private @Nullable ConcreteInterfaceAddress _ipAddress;
  private @Nullable InterfaceLldp _lldp;
  private @Nullable Integer _mtu;
  private @Nullable String _name;
  private final int _number;
  private @Nullable TrunkGroup _trunkGroup;
}
