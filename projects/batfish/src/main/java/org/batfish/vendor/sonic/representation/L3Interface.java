package org.batfish.vendor.sonic.representation;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;

public class L3Interface implements Serializable {
  private final ConcreteInterfaceAddress _address;

  public L3Interface(@Nullable ConcreteInterfaceAddress address) {
    _address = address;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof L3Interface)) {
      return false;
    }
    L3Interface that = (L3Interface) o;
    return Objects.equals(_address, that._address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_address);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("address", _address).toString();
  }
}
