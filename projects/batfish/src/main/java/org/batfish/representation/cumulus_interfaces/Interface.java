package org.batfish.representation.cumulus_interfaces;

import com.google.common.collect.ImmutableList;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/** Model of an iface block in a cumulus /etc/network/interfaces file. */
public final class Interface {
  private final @Nonnull List<ConcreteInterfaceAddress> _addresses = new LinkedList<>();
  private final @Nonnull String _name;
  private @Nullable String _vrf;
  private @Nullable List<String> _bondSlaves;

  public Interface(@Nonnull String name) {
    _name = name;
  }

  public void addAddress(ConcreteInterfaceAddress address) {
    _addresses.add(address);
  }

  public List<ConcreteInterfaceAddress> getAddresses() {
    return _addresses;
  }

  @Nullable
  public List<String> getBondSlaves() {
    return _bondSlaves;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nullable
  public String getVrf() {
    return _vrf;
  }

  public void setBondSlaves(List<String> bondSlaves) {
    _bondSlaves = ImmutableList.copyOf(bondSlaves);
  }

  public void setVrf(String vrf) {
    _vrf = vrf;
  }
}
