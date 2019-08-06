package org.batfish.representation.cumulus_interfaces;

import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/** Model of an iface block in a cumulus /etc/network/interfaces file. */
public final class Interface {
  private final @Nonnull String _name;
  private final @Nonnull List<ConcreteInterfaceAddress> _addresses = new LinkedList<>();

  public Interface(@Nonnull String name) {
    _name = name;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  public void addAddress(ConcreteInterfaceAddress address) {
    _addresses.add(address);
  }

  public List<ConcreteInterfaceAddress> getAddresses() {
    return _addresses;
  }
}
