package org.batfish.vendor.sonic.representation;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.util.CollectionUtil;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/**
 * Represents an L3 interface that appears under INTERFACE, PORT_CHANNEL, or VLAN objects in
 * configdb.
 *
 * <p>This object is created by parsing the multi-level key encoding in those objects.
 */
public class L3Interface implements Serializable {
  private @Nonnull Map<ConcreteInterfaceAddress, InterfaceKeyProperties> _addresses;

  public L3Interface(Map<ConcreteInterfaceAddress, InterfaceKeyProperties> addresses) {
    _addresses = ImmutableMap.copyOf(addresses);
  }

  public void addAddress(ConcreteInterfaceAddress address, InterfaceKeyProperties properties) {
    _addresses = CollectionUtil.copyMapAndAdd(_addresses, address, properties);
  }

  public @Nonnull Map<ConcreteInterfaceAddress, InterfaceKeyProperties> getAddresses() {
    return _addresses;
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
    return _addresses.equals(that._addresses);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_addresses);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("addresses", _addresses).toString();
  }
}
