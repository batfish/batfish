package org.batfish.representation.cumulus;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.InterfaceAddress;

/** A logical layer-1 bond interface */
public class Bond implements Serializable {

  private final @Nonnull InterfaceBridgeSettings _bridge;
  private @Nullable Integer _clagId;
  private final @Nonnull List<InterfaceAddress> _ipAddresses;
  private final @Nonnull String _name;
  private @Nonnull Set<String> _slaves;
  private @Nullable String _vrf;

  public Bond(String name) {
    _name = name;
    _bridge = new InterfaceBridgeSettings();
    _ipAddresses = new LinkedList<>();
    _slaves = ImmutableSet.of();
  }

  public @Nonnull InterfaceBridgeSettings getBridge() {
    return _bridge;
  }

  public @Nullable Integer getClagId() {
    return _clagId;
  }

  public @Nonnull List<InterfaceAddress> getIpAddresses() {
    return _ipAddresses;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Set<String> getSlaves() {
    return _slaves;
  }

  public @Nullable String getVrf() {
    return _vrf;
  }

  public void setClagId(@Nullable Integer clagId) {
    _clagId = clagId;
  }

  public void setSlaves(Set<String> slaves) {
    _slaves = ImmutableSet.copyOf(slaves);
  }

  public void setVrf(String vrf) {
    _vrf = vrf;
  }
}
