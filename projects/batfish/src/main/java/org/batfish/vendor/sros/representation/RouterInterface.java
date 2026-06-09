package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;

/**
 * An SR-OS router interface (e.g. {@code router "Base" interface "system"}), keyed by its
 * interface-name string. The "system" and loopback interfaces need no port binding; others bind to
 * a {@link Port}. The IPv4 primary address is modeled as an {@link Ip} plus prefix-length, since
 * SR-OS configures the two as separate leaves.
 */
public final class RouterInterface implements Serializable {

  public RouterInterface(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  /**
   * The bound port path (e.g. {@code 1/1/c1/1}), or {@code null} for system/loopback interfaces.
   */
  public @Nullable String getPort() {
    return _port;
  }

  public void setPort(@Nullable String port) {
    _port = port;
  }

  /** The {@code ipv4 primary address}, or {@code null} if unset. */
  public @Nullable Ip getPrimaryAddress() {
    return _primaryAddress;
  }

  public void setPrimaryAddress(@Nullable Ip primaryAddress) {
    _primaryAddress = primaryAddress;
  }

  /** The {@code ipv4 primary prefix-length}, or {@code null} if unset. */
  public @Nullable Integer getPrimaryPrefixLength() {
    return _primaryPrefixLength;
  }

  public void setPrimaryPrefixLength(@Nullable Integer primaryPrefixLength) {
    _primaryPrefixLength = primaryPrefixLength;
  }

  /**
   * The combined primary {@link ConcreteInterfaceAddress} if both address and prefix-length are
   * set, else {@code null}.
   */
  public @Nullable ConcreteInterfaceAddress getPrimaryConcreteAddress() {
    if (_primaryAddress == null || _primaryPrefixLength == null) {
      return null;
    }
    return ConcreteInterfaceAddress.create(_primaryAddress, _primaryPrefixLength);
  }

  private final @Nonnull String _name;
  private @Nullable String _port;
  private @Nullable Ip _primaryAddress;
  private @Nullable Integer _primaryPrefixLength;
}
