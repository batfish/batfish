package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.InterfaceAddress;

/** A layer-2- or layer-3-capable network interface */
public final class Interface implements Serializable {

  private @Nullable InterfaceAddress _address;
  private final @Nonnull Set<String> _declaredNames;
  private final @Nonnull String _name;
  private final @Nullable String _parentInterface;
  private final @Nonnull Set<InterfaceAddress> _secondaryAddresses;
  private boolean _shutdown;
  private @Nullable String _vrfMember;

  public Interface(String name, String parentInterface) {
    _name = name;
    _parentInterface = parentInterface;
    _declaredNames = new HashSet<>();
    _secondaryAddresses = new HashSet<>();
    _shutdown = true;
  }

  /** The primary IPv4 address of the interface. */
  public @Nullable InterfaceAddress getAddress() {
    return _address;
  }

  public @Nonnull Set<String> getDeclaredNames() {
    return _declaredNames;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getParentInterface() {
    return _parentInterface;
  }

  /** The set of secondary IPv4 addresses of the interface. */
  public @Nonnull Set<InterfaceAddress> getSecondaryAddresses() {
    return _secondaryAddresses;
  }

  public boolean getShutdown() {
    return _shutdown;
  }

  public @Nullable String getVrfMember() {
    return _vrfMember;
  }

  public void setAddress(InterfaceAddress address) {
    _address = address;
  }

  public void setShutdown(boolean shutdown) {
    _shutdown = shutdown;
  }
}
