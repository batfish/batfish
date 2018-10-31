package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;

public final class InterfaceAddress implements Comparable<InterfaceAddress>, Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private Ip _ip;

  private int _networkBits;

  public InterfaceAddress(@Nonnull Ip ip, int networkBits) {
    _ip = ip;
    _networkBits = networkBits;
  }

  public InterfaceAddress(@Nonnull Ip ip, @Nonnull Ip networkMask) {
    _ip = ip;
    _networkBits = networkMask.numSubnetBits();
  }

  @JsonCreator
  public InterfaceAddress(@Nullable String text) {
    if (text == null) {
      throw new BatfishException("Cannot create InterfaceAddress from null string");
    }
    String[] parts = text.split("/");
    if (parts.length != 2) {
      throw new BatfishException(
          String.format("Invalid %s string: \"%s\"", InterfaceAddress.class.getSimpleName(), text));
    }
    _ip = new Ip(parts[0]);
    try {
      _networkBits = Integer.parseInt(parts[1]);
    } catch (NumberFormatException e) {
      throw new BatfishException("Invalid network length: \"" + parts[1] + "\"", e);
    }
  }

  @Override
  public int compareTo(InterfaceAddress rhs) {
    int ret = _ip.compareTo(rhs._ip);
    if (ret != 0) {
      return ret;
    }
    return Integer.compare(_networkBits, rhs._networkBits);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof InterfaceAddress)) {
      return false;
    }
    InterfaceAddress rhs = (InterfaceAddress) o;
    return _ip.equals(rhs._ip) && _networkBits == rhs._networkBits;
  }

  public Ip getIp() {
    return _ip;
  }

  public int getNetworkBits() {
    return _networkBits;
  }

  public Prefix getPrefix() {
    return new Prefix(_ip, _networkBits);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ip, _networkBits);
  }

  @Override
  @JsonValue
  public String toString() {
    return _ip + "/" + _networkBits;
  }
}
