package org.batfish.vendor.sonic.representation;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/**
 * Represents an L3 interface that appears under INTERFACE, PORT_CHANNEL, or VLAN objects in
 * configdb.
 *
 * <p>This object is created by parsing the multi-level key encoding in those objects.
 */
public class L3Interface implements Serializable {
  // TODO: Does Sonic permit multiple addresses?
  private final @Nullable ConcreteInterfaceAddress _address;

  public L3Interface(@Nullable ConcreteInterfaceAddress address) {
    _address = address;
  }

  public @Nullable ConcreteInterfaceAddress getAddress() {
    return _address;
  }

  @Override
  public boolean equals(@Nullable Object o) {
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
    return MoreObjects.toStringHelper(this).omitNullValues().add("address", _address).toString();
  }
}
