package org.batfish.vendor.a10.representation;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/** Datamodel class representing a configured A10 interface. */
public final class Interface implements Serializable {
  public enum Type {
    ETHERNET,
    LOOPBACK,
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

  public int getMtuEffective() {
    return firstNonNull(_mtu, DEFAULT_MTU);
  }

  @Nullable
  public String getName() {
    return _name;
  }

  public int getNumber() {
    return _number;
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

  public Interface(Type type, int number) {
    _number = number;
    _type = type;
  }

  @Nullable private Boolean _enabled;
  @Nonnull private final Type _type;
  @Nullable private ConcreteInterfaceAddress _ipAddress;
  @Nullable private Integer _mtu;
  @Nullable private String _name;
  private final int _number;
}
