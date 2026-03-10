package org.batfish.datamodel.vendor_family.f5_bigip;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Objects;
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

  public static final class Builder {

    public @Nonnull Virtual build() {
      checkArgument(_name != null, "Missing name");
      return new Virtual(
          _description,
          _destination,
          _destinationPort,
          _disabled,
          _ipForward,
          _ipProtocol,
          _mask,
          _mask6,
          _name,
          _pool,
          _reject,
          _source,
          _source6,
          _sourceAddressTranslationPool,
          _translateAddress,
          _translatePort,
          _vlans.build(),
          _vlansEnabled);
    }

    public @Nonnull Builder setDescription(@Nullable String description) {
      _description = description;
      return this;
    }

    public @Nonnull Builder setDestination(@Nullable String destination) {
      _destination = destination;
      return this;
    }

    public @Nonnull Builder setDestinationPort(@Nullable Integer destinationPort) {
      _destinationPort = destinationPort;
      return this;
    }

    public @Nonnull Builder setDisabled(@Nullable Boolean disabled) {
      _disabled = disabled;
      return this;
    }

    public @Nonnull Builder setIpForward(boolean ipForward) {
      _ipForward = ipForward;
      return this;
    }

    public @Nonnull Builder setIpProtocol(@Nullable IpProtocol ipProtocol) {
      _ipProtocol = ipProtocol;
      return this;
    }

    public @Nonnull Builder setMask(@Nullable Ip mask) {
      _mask = mask;
      return this;
    }

    public @Nonnull Builder setMask6(@Nullable Ip6 mask6) {
      _mask6 = mask6;
      return this;
    }

    public @Nonnull Builder setName(String name) {
      _name = name;
      return this;
    }

    public @Nonnull Builder setPool(@Nullable String pool) {
      _pool = pool;
      return this;
    }

    public @Nonnull Builder setReject(boolean reject) {
      _reject = reject;
      return this;
    }

    public @Nonnull Builder setSource(@Nullable Prefix source) {
      _source = source;
      return this;
    }

    public @Nonnull Builder setSource6(@Nullable Prefix6 source6) {
      _source6 = source6;
      return this;
    }

    public @Nonnull Builder setSourceAddressTranslationPool(
        @Nullable String sourceAddressTranslationPool) {
      _sourceAddressTranslationPool = sourceAddressTranslationPool;
      return this;
    }

    public @Nonnull Builder setTranslateAddress(@Nullable Boolean translateAddress) {
      _translateAddress = translateAddress;
      return this;
    }

    public @Nonnull Builder setTranslatePort(@Nullable Boolean translatePort) {
      _translatePort = translatePort;
      return this;
    }

    public @Nonnull Builder addVlan(String vlan) {
      _vlans.add(vlan);
      return this;
    }

    public @Nonnull Builder setVlans(Iterable<String> vlans) {
      _vlans = ImmutableSet.<String>builder().addAll(vlans);
      return this;
    }

    public @Nonnull Builder setVlansEnabled(boolean vlansEnabled) {
      _vlansEnabled = vlansEnabled;
      return this;
    }

    private @Nullable String _description;
    private @Nullable String _destination;
    private @Nullable Integer _destinationPort;
    private @Nullable Boolean _disabled;
    private boolean _ipForward;
    private @Nullable IpProtocol _ipProtocol;
    private @Nullable Ip _mask;
    private @Nullable Ip6 _mask6;
    private @Nullable String _name;
    private @Nullable String _pool;
    private boolean _reject;
    private @Nullable Prefix _source;
    private @Nullable Prefix6 _source6;
    private @Nullable String _sourceAddressTranslationPool;
    private @Nullable Boolean _translateAddress;
    private @Nullable Boolean _translatePort;
    private @Nonnull ImmutableSet.Builder<String> _vlans;
    private boolean _vlansEnabled;

    private Builder() {
      _vlans = ImmutableSet.builder();
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
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

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Virtual)) {
      return false;
    }
    Virtual rhs = (Virtual) obj;
    return Objects.equals(_description, rhs._description)
        && Objects.equals(_destination, rhs._destination)
        && Objects.equals(_destinationPort, rhs._destinationPort)
        && Objects.equals(_disabled, rhs._disabled)
        && _ipForward == rhs._ipForward
        && Objects.equals(_ipProtocol, rhs._ipProtocol)
        && Objects.equals(_mask, rhs._mask)
        && Objects.equals(_mask6, rhs._mask6)
        && _name.equals(rhs._name)
        && Objects.equals(_pool, rhs._pool)
        && _reject == rhs._reject
        && Objects.equals(_source, rhs._source)
        && Objects.equals(_source6, rhs._source6)
        && Objects.equals(_sourceAddressTranslationPool, rhs._sourceAddressTranslationPool)
        && Objects.equals(_translateAddress, rhs._translateAddress)
        && Objects.equals(_translatePort, rhs._translatePort)
        && _vlans.equals(rhs._vlans)
        && _vlansEnabled == rhs._vlansEnabled;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _description,
        _destination,
        _destinationPort,
        _disabled,
        _ipForward,
        _ipProtocol != null ? _ipProtocol.ordinal() : null,
        _mask,
        _mask6,
        _name,
        _pool,
        _reject,
        _source,
        _source6,
        _sourceAddressTranslationPool,
        _translateAddress,
        _translatePort,
        _vlans,
        _vlansEnabled);
  }

  private final @Nullable String _description;
  private final @Nullable String _destination;
  private final @Nullable Integer _destinationPort;
  private final @Nullable Boolean _disabled;
  private final boolean _ipForward;
  private final @Nullable IpProtocol _ipProtocol;
  private final @Nullable Ip _mask;
  private final @Nullable Ip6 _mask6;
  private final @Nonnull String _name;
  private final @Nullable String _pool;
  private final boolean _reject;
  private final @Nullable Prefix _source;
  private final @Nullable Prefix6 _source6;
  private final @Nullable String _sourceAddressTranslationPool;
  private final @Nullable Boolean _translateAddress;
  private final @Nullable Boolean _translatePort;
  private final @Nonnull Set<String> _vlans;
  private final boolean _vlansEnabled;

  private Virtual(
      @Nullable String description,
      @Nullable String destination,
      @Nullable Integer destinationPort,
      @Nullable Boolean disabled,
      boolean ipForward,
      @Nullable IpProtocol ipProtocol,
      @Nullable Ip mask,
      @Nullable Ip6 mask6,
      String name,
      @Nullable String pool,
      boolean reject,
      @Nullable Prefix source,
      @Nullable Prefix6 source6,
      @Nullable String sourceAddressTranslationPool,
      @Nullable Boolean translateAddress,
      @Nullable Boolean translatePort,
      Set<String> vlans,
      boolean vlansEnabled) {
    _description = description;
    _destination = destination;
    _destinationPort = destinationPort;
    _disabled = disabled;
    _ipForward = ipForward;
    _ipProtocol = ipProtocol;
    _mask = mask;
    _mask6 = mask6;
    _name = name;
    _pool = pool;
    _reject = reject;
    _source = source;
    _source6 = source6;
    _sourceAddressTranslationPool = sourceAddressTranslationPool;
    _translateAddress = translateAddress;
    _translatePort = translatePort;
    _vlans = vlans;
    _vlansEnabled = vlansEnabled;
  }
}
