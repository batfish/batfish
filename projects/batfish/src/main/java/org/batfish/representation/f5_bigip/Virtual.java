package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;

/** Configuration for a pool of virtual services. */
public final class Virtual implements Serializable {

  private @Nullable String _description;
  private @Nullable String _destination;
  private @Nullable Integer _destinationPort;
  private @Nullable Boolean _disabled;
  private boolean _ipForward;
  private @Nullable IpProtocol _ipProtocol;
  private @Nullable Ip _mask;
  private @Nullable Ip6 _mask6;
  private final @Nonnull String _name;
  private @Nullable String _pool;
  private boolean _reject;
  private @Nullable Prefix _source;
  private @Nullable Prefix6 _source6;
  private @Nullable String _sourceAddressTranslationPool;
  private @Nullable Boolean _translateAddress;
  private @Nullable Boolean _translatePort;
  private final @Nonnull Set<String> _vlans;
  private boolean _vlansEnabled;

  public Virtual(String name) {
    _name = name;
    _vlans = new HashSet<>();
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nullable String getDestination() {
    return _destination;
  }

  public @Nullable Integer getDestinationPort() {
    return _destinationPort;
  }

  public @Nullable Boolean getDisabled() {
    return _disabled;
  }

  public boolean getIpForward() {
    return _ipForward;
  }

  public @Nullable IpProtocol getIpProtocol() {
    return _ipProtocol;
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

  public boolean getReject() {
    return _reject;
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

  public @Nullable Boolean getTranslateAddress() {
    return _translateAddress;
  }

  public @Nullable Boolean getTranslatePort() {
    return _translatePort;
  }

  public @Nonnull Set<String> getVlans() {
    return _vlans;
  }

  public boolean getVlansEnabled() {
    return _vlansEnabled;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  public void setDestination(@Nullable String destination) {
    _destination = destination;
  }

  public void setDestinationPort(@Nullable Integer destinationPort) {
    _destinationPort = destinationPort;
  }

  public void setDisabled(@Nullable Boolean disabled) {
    _disabled = disabled;
  }

  public void setIpForward(boolean ipForward) {
    _ipForward = ipForward;
  }

  public void setIpProtocol(@Nullable IpProtocol ipProtocol) {
    _ipProtocol = ipProtocol;
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

  public void setReject(boolean reject) {
    _reject = reject;
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

  public void setTranslateAddress(@Nullable Boolean translateAddress) {
    _translateAddress = translateAddress;
  }

  public void setTranslatePort(@Nullable Boolean translatePort) {
    _translatePort = translatePort;
  }

  public void setVlansEnabled(boolean vlansEnabled) {
    _vlansEnabled = vlansEnabled;
  }
}
