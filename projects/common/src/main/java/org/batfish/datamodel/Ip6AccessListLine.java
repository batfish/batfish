package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** One ordered IPv6 access-list entry. */
@ParametersAreNonnullByDefault
public final class Ip6AccessListLine implements Serializable {

  private static final String PROP_ACTION = "action";
  private static final String PROP_DST_PORTS = "dstPorts";
  private static final String PROP_DST_PREFIX = "dstPrefix";
  private static final String PROP_ICMP_CODE = "icmpCode";
  private static final String PROP_ICMP_TYPE = "icmpType";
  private static final String PROP_NAME = "name";
  private static final String PROP_PROTOCOL = "protocol";
  private static final String PROP_SRC_PORTS = "srcPorts";
  private static final String PROP_SRC_PREFIX = "srcPrefix";

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

  @JsonCreator
  private static Ip6AccessListLine create(
      @JsonProperty(PROP_NAME)
          @Nullable String name,
      @JsonProperty(PROP_ACTION)
          @Nullable LineAction action,
      @JsonProperty(PROP_SRC_PREFIX)
          @Nullable Prefix6 srcPrefix,
      @JsonProperty(PROP_DST_PREFIX)
          @Nullable Prefix6 dstPrefix,
      @JsonProperty(PROP_PROTOCOL)
          @Nullable IpProtocol protocol,
      @JsonProperty(PROP_SRC_PORTS)
          @Nullable SubRange srcPorts,
      @JsonProperty(PROP_DST_PORTS)
          @Nullable SubRange dstPorts,
      @JsonProperty(PROP_ICMP_TYPE)
          @Nullable Integer icmpType,
      @JsonProperty(PROP_ICMP_CODE)
          @Nullable Integer icmpCode) {

    checkArgument(
        action != null,
        "IPv6 ACL line missing %s",
        PROP_ACTION);

    return new Ip6AccessListLine(
        name,
        action,
        srcPrefix,
        dstPrefix,
        protocol,
        srcPorts,
        dstPorts,
        icmpType,
        icmpCode);
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
        && (flow.getSrcPort() == null
            || !_srcPorts.includes(flow.getSrcPort()))) {
      return false;
    }

    if (_dstPorts != null
        && (flow.getDstPort() == null
            || !_dstPorts.includes(flow.getDstPort()))) {
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

  @JsonProperty(PROP_ACTION)
  public @Nonnull LineAction getAction() {
    return _action;
  }

  @JsonProperty(PROP_DST_PREFIX)
  public @Nullable Prefix6 getDstPrefix() {
    return _dstPrefix;
  }

  @JsonProperty(PROP_DST_PORTS)
  public @Nullable SubRange getDstPorts() {
    return _dstPorts;
  }

  @JsonProperty(PROP_ICMP_CODE)
  public @Nullable Integer getIcmpCode() {
    return _icmpCode;
  }

  @JsonProperty(PROP_ICMP_TYPE)
  public @Nullable Integer getIcmpType() {
    return _icmpType;
  }

  @JsonProperty(PROP_NAME)
  public @Nullable String getName() {
    return _name;
  }

  @JsonProperty(PROP_PROTOCOL)
  public @Nullable IpProtocol getProtocol() {
    return _protocol;
  }

  @JsonProperty(PROP_SRC_PREFIX)
  public @Nullable Prefix6 getSrcPrefix() {
    return _srcPrefix;
  }

  @JsonProperty(PROP_SRC_PORTS)
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
