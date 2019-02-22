package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;

/** Configuration for a pool of nodes. */
@ParametersAreNonnullByDefault
public final class Virtual implements Serializable {

  private static final long serialVersionUID = 1L;

  private @Nullable Ip _destinationIp;
  private @Nullable Ip6 _destinationIp6;
  private @Nullable Integer _destinationPort;
  private @Nullable Ip _mask;
  private @Nullable Ip6 _mask6;
  private final @Nonnull String _name;
  private @Nullable String _pool;
  private @Nullable Prefix _source;
  private @Nullable Prefix6 _source6;
  private @Nullable String _sourceAddressTranslationPool;

  public Virtual(String name) {
    _name = name;
  }

  public @Nullable Ip getDestinationIp() {
    return _destinationIp;
  }

  public @Nullable Ip6 getDestinationIp6() {
    return _destinationIp6;
  }

  public @Nullable Integer getDestinationPort() {
    return _destinationPort;
  }

  public @Nullable Ip getMask() {
    return _mask;
  }

  public @Nullable Ip6 getMask6() {
    return _mask6;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getPool() {
    return _pool;
  }

  public @Nullable Prefix getSource() {
    return _source;
  }

  public @Nullable Prefix6 getSource6() {
    return _source6;
  }

  public @Nullable String getSourceAddressTranslationPool() {
    return _sourceAddressTranslationPool;
  }

  public void setDestinationIp(@Nullable Ip destinationIp) {
    _destinationIp = destinationIp;
  }

  public void setDestinationIp6(@Nullable Ip6 destinationIp6) {
    _destinationIp6 = destinationIp6;
  }

  public void setDestinationPort(@Nullable Integer destinationPort) {
    _destinationPort = destinationPort;
  }

  public void setMask(@Nullable Ip mask) {
    _mask = mask;
  }

  public void setMask6(@Nullable Ip6 mask6) {
    _mask6 = mask6;
  }

  public void setPool(@Nullable String pool) {
    _pool = pool;
  }

  public void setSource(@Nullable Prefix source) {
    _source = source;
  }

  public void setSource6(@Nullable Prefix6 source6) {
    _source6 = source6;
  }

  public void setSourceAddressTranslationPool(@Nullable String sourceAddressTranslationPool) {
    _sourceAddressTranslationPool = sourceAddressTranslationPool;
  }
}
