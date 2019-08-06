package org.batfish.representation.cumulus_interfaces;

import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/** Model of an iface block in a cumulus /etc/network/interfaces file. */
public final class Interface {
  private final @Nonnull List<ConcreteInterfaceAddress> _addresses = new LinkedList<>();
  private final @Nonnull String _name;
  private String _vrf;

  public Interface(@Nonnull String name) {
    _name = name;
  }

  public void addAddress(ConcreteInterfaceAddress address) {
    _addresses.add(address);
  }

  public List<ConcreteInterfaceAddress> getAddresses() {
    return _addresses;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  public String getVrf() {
    return _vrf;
  }

  public void setVrf(String vrf) {
    _vrf = vrf;
  }
}
