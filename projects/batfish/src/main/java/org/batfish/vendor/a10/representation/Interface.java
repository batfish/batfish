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

  @Nullable
  public Boolean getEnabled() {
    return _enabled;
  }

  @Nullable
  public ConcreteInterfaceAddress getIpAddress() {
    return _ipAddress;
  }

  @Nullable
  public Integer getMtu() {
    return _mtu;
  }

  @Nullable
  public String getName() {
    return _name;
  }

  public int getNumber() {
    return _number;
  }

  public TrunkGroup getTrunkGroup() {
    return _trunkGroup;
  }

  @Nonnull
  public Type getType() {
    return _type;
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

  @Nullable
  public InterfaceLldp getLldp() {
    return _lldp;
  }

  /** Get LLDP configuration object for this interface, creating one if it doesn't already exist. */
  @Nonnull
  public InterfaceLldp getOrCreateLldp() {
    if (_lldp == null) {
      _lldp = new InterfaceLldp();
    }
    return _lldp;
  }

  public Interface(Type type, int number) {
    _number = number;
    _type = type;
  }

  @Nullable private Boolean _enabled;
  @Nonnull private final Type _type;
  @Nullable private ConcreteInterfaceAddress _ipAddress;
  @Nullable private InterfaceLldp _lldp;
  @Nullable private Integer _mtu;
  @Nullable private String _name;
  private final int _number;
  @Nullable private TrunkGroup _trunkGroup;
}
