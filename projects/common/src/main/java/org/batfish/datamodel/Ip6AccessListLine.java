package org.batfish.datamodel;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** One ordered IPv6 access-list entry. */
@ParametersAreNonnullByDefault
public final class Ip6AccessListLine implements Serializable {

  public static final class Builder {

    private @Nonnull LineAction _action;
    private @Nullable Prefix6 _dstPrefix;
    private @Nullable SubRange _dstPorts;
    private @Nullable Integer _icmpCode;
    private @Nullable Integer _icmpType;
    private @Nullable String _name;
    private @Nullable IpProtocol _protocol;
    private @Nullable Prefix6 _srcPrefix;
    private @Nullable SubRange _srcPorts;

    private Builder() {
      _action = LineAction.DENY;
    }

    public Ip6AccessListLine build() {
      return new Ip6AccessListLine(
          _name,
          _action,
          _srcPrefix,
          _dstPrefix,
          _protocol,
          _srcPorts,
          _dstPorts,
          _icmpType,
          _icmpCode);
    }

    public Builder setAction(LineAction action) {
      _action = action;
      return this;
    }

    public Builder setDstPrefix(
        @Nullable Prefix6 dstPrefix) {
      _dstPrefix = dstPrefix;
      return this;
    }

    public Builder setDstPorts(
        @Nullable SubRange dstPorts) {
      _dstPorts = dstPorts;
      return this;
    }

    public Builder setIcmpCode(
        @Nullable Integer icmpCode) {
      _icmpCode = icmpCode;
      return this;
    }

    public Builder setIcmpType(
        @Nullable Integer icmpType) {
      _icmpType = icmpType;
      return this;
    }

    public Builder setName(
        @Nullable String name) {
      _name = name;
      return this;
    }

    public Builder setProtocol(
        @Nullable IpProtocol protocol) {
      _protocol = protocol;
      return this;
    }

    public Builder setSrcPrefix(
        @Nullable Prefix6 srcPrefix) {
      _srcPrefix = srcPrefix;
      return this;
    }

    public Builder setSrcPorts(
        @Nullable SubRange srcPorts) {
      _srcPorts = srcPorts;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private Ip6AccessListLine(
      @Nullable String name,
      LineAction action,
      @Nullable Prefix6 srcPrefix,
      @Nullable Prefix6 dstPrefix,
      @Nullable IpProtocol protocol,
      @Nullable SubRange srcPorts,
      @Nullable SubRange dstPorts,
      @Nullable Integer icmpType,
      @Nullable Integer icmpCode) {
    _name = name;
    _action = action;
    _srcPrefix = srcPrefix;
    _dstPrefix = dstPrefix;
    _protocol = protocol;
    _srcPorts = srcPorts;
    _dstPorts = dstPorts;
    _icmpType = icmpType;
    _icmpCode = icmpCode;
  }

  public boolean matches(Flow6 flow) {
    if (_srcPrefix != null
        && !_srcPrefix.contains(flow.getSrcIp())) {
      return false;
    }

    if (_dstPrefix != null
        && !_dstPrefix.contains(flow.getDstIp())) {
      return false;
    }

    if (_protocol != null
        && _protocol != flow.getIpProtocol()) {
      return false;
    }

    if (_srcPorts != null
        && !_srcPorts.includes(flow.getSrcPort())) {
      return false;
    }

    if (_dstPorts != null
        && !_dstPorts.includes(flow.getDstPort())) {
      return false;
    }

    if (_icmpType != null
        && !Objects.equals(
            _icmpType,
            flow.getIcmpType())) {
      return false;
    }

    return _icmpCode == null
        || Objects.equals(
            _icmpCode,
            flow.getIcmpCode());
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nullable Prefix6 getDstPrefix() {
    return _dstPrefix;
  }

  public @Nullable SubRange getDstPorts() {
    return _dstPorts;
  }

  public @Nullable Integer getIcmpCode() {
    return _icmpCode;
  }

  public @Nullable Integer getIcmpType() {
    return _icmpType;
  }

  public @Nullable String getName() {
    return _name;
  }

  public @Nullable IpProtocol getProtocol() {
    return _protocol;
  }

  public @Nullable Prefix6 getSrcPrefix() {
    return _srcPrefix;
  }

  public @Nullable SubRange getSrcPorts() {
    return _srcPorts;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Ip6AccessListLine)) {
      return false;
    }

    Ip6AccessListLine rhs =
        (Ip6AccessListLine) o;

    return _action == rhs._action
        && Objects.equals(_name, rhs._name)
        && Objects.equals(
            _srcPrefix, rhs._srcPrefix)
        && Objects.equals(
            _dstPrefix, rhs._dstPrefix)
        && _protocol == rhs._protocol
        && Objects.equals(
            _srcPorts, rhs._srcPorts)
        && Objects.equals(
            _dstPorts, rhs._dstPorts)
        && Objects.equals(
            _icmpType, rhs._icmpType)
        && Objects.equals(
            _icmpCode, rhs._icmpCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _name,
        _action,
        _srcPrefix,
        _dstPrefix,
        _protocol,
        _srcPorts,
        _dstPorts,
        _icmpType,
        _icmpCode);
  }

  private final @Nonnull LineAction _action;
  private final @Nullable Prefix6 _dstPrefix;
  private final @Nullable SubRange _dstPorts;
  private final @Nullable Integer _icmpCode;
  private final @Nullable Integer _icmpType;
  private final @Nullable String _name;
  private final @Nullable IpProtocol _protocol;
  private final @Nullable Prefix6 _srcPrefix;
  private final @Nullable SubRange _srcPorts;
}
