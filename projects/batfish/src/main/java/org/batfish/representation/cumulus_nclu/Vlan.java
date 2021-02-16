package org.batfish.representation.cumulus_nclu;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.MacAddress;

/** Layer-3 integrated routing and bridging VLAN interface */
public class Vlan implements Serializable {

  private final @Nonnull List<InterfaceAddress> _addresses;
  private final @Nonnull Map<MacAddress, Set<InterfaceAddress>> _addressVirtuals;
  private @Nullable String _alias;
  private final @Nonnull String _name;
  private @Nullable Integer _vlanId;
  private @Nullable String _vrf;

  public Vlan(String name) {
    _name = name;
    _addresses = new LinkedList<>();
    _addressVirtuals = new HashMap<>();
  }

  public @Nonnull List<InterfaceAddress> getAddresses() {
    return _addresses;
  }

  public @Nonnull Map<MacAddress, Set<InterfaceAddress>> getAddressVirtuals() {
    return _addressVirtuals;
  }

  @Nullable
  public String getAlias() {
    return _alias;
  }

  public void setAlias(@Nullable String alias) {
    _alias = alias;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Integer getVlanId() {
    return _vlanId;
  }

  public @Nullable String getVrf() {
    return _vrf;
  }

  public void setVlanId(@Nullable Integer vlanId) {
    _vlanId = vlanId;
  }

  public void setVrf(@Nullable String vrf) {
    _vrf = vrf;
  }
}
