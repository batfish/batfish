package org.batfish.representation.cisco_xr;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip6Wildcard;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlagsMatchConditions;

@ParametersAreNonnullByDefault
public class Ipv6AccessListLine implements Serializable {

  public static class Builder {

    private @Nullable LineAction _action;

    private @Nullable Set<Integer> _dscps;

    private @Nullable String _dstAddressGroup;

    private @Nullable Ip6Wildcard _dstIpWildcard;

    private @Nullable List<SubRange> _dstPortRanges;

    private @Nullable Integer _icmpCode;

    private @Nullable Integer _icmpType;

    private @Nullable String _name;

    private @Nullable Ipv6Nexthop _nexthop1;

    private @Nullable Ipv6Nexthop _nexthop2;

    private @Nullable Ipv6Nexthop _nexthop3;

    private @Nullable IpProtocol _protocol;

    private @Nullable String _srcAddressGroup;

    private @Nullable Ip6Wildcard _srcIpWildcard;

    private @Nullable List<SubRange> _srcPortRanges;

    private @Nullable List<TcpFlagsMatchConditions> _tcpFlags;

    private Builder() {}

    public Ipv6AccessListLine build() {
      return new Ipv6AccessListLine(this);
    }

    public Ipv6AccessListLine.Builder setAction(LineAction action) {
      _action = action;
      return this;
    }

    public Ipv6AccessListLine.Builder setDscps(Set<Integer> dscps) {
      _dscps = dscps;
      return this;
    }

    public Ipv6AccessListLine.Builder setDstAddressGroup(String dstAddressGroup) {
      _dstAddressGroup = dstAddressGroup;
      return this;
    }

    public Ipv6AccessListLine.Builder setDstIpWildcard(Ip6Wildcard dstIpWildcard) {
      _dstIpWildcard = dstIpWildcard;
      return this;
    }

    public Ipv6AccessListLine.Builder setDstPortRanges(List<SubRange> dstPortRanges) {
      _dstPortRanges = dstPortRanges;
      return this;
    }

    public Ipv6AccessListLine.Builder setIcmpCode(Integer icmpCode) {
      _icmpCode = icmpCode;
      return this;
    }

    public Ipv6AccessListLine.Builder setIcmpType(Integer icmpType) {
      _icmpType = icmpType;
      return this;
    }

    public Ipv6AccessListLine.Builder setName(String name) {
      _name = name;
      return this;
    }

    public Ipv6AccessListLine.Builder setNexthop1(Ipv6Nexthop nexthop) {
      _nexthop1 = nexthop;
      return this;
    }

    public Ipv6AccessListLine.Builder setNexthop2(Ipv6Nexthop nexthop) {
      _nexthop2 = nexthop;
      return this;
    }

    public Ipv6AccessListLine.Builder setNexthop3(Ipv6Nexthop nexthop) {
      _nexthop3 = nexthop;
      return this;
    }

    public Ipv6AccessListLine.Builder setProtocol(IpProtocol protocol) {
      _protocol = protocol;
      return this;
    }

    public Ipv6AccessListLine.Builder setSrcAddressGroup(String srcAddressGroup) {
      _srcAddressGroup = srcAddressGroup;
      return this;
    }

    public Ipv6AccessListLine.Builder setSrcIpWildcard(Ip6Wildcard srcIpWildcard) {
      _srcIpWildcard = srcIpWildcard;
      return this;
    }

    public Ipv6AccessListLine.Builder setSrcPortRanges(List<SubRange> srcPortRanges) {
      _srcPortRanges = srcPortRanges;
      return this;
    }

    public Ipv6AccessListLine.Builder setTcpFlags(List<TcpFlagsMatchConditions> tcpFlags) {
      _tcpFlags = tcpFlags;
      return this;
    }
  }

  public static Ipv6AccessListLine.Builder builder() {
    return new Ipv6AccessListLine.Builder();
  }

  private final @Nonnull LineAction _action;

  private final @Nonnull Set<Integer> _dscps;

  private final @Nullable String _dstAddressGroup;

  private final @Nonnull Ip6Wildcard _dstIpWildcard;

  private final @Nonnull List<SubRange> _dstPortRanges;

  private final @Nullable Integer _icmpCode;

  private final @Nullable Integer _icmpType;

  private final @Nonnull String _name;

  private final @Nullable Ipv6Nexthop _nexthop1;

  private final @Nullable Ipv6Nexthop _nexthop2;

  private final @Nullable Ipv6Nexthop _nexthop3;

  private final @Nullable IpProtocol _protocol;

  private final @Nullable String _srcAddressGroup;

  private final @Nonnull Ip6Wildcard _srcIpWildcard;

  private final @Nonnull List<SubRange> _srcPortRanges;

  private final @Nonnull List<TcpFlagsMatchConditions> _tcpFlags;

  private Ipv6AccessListLine(Ipv6AccessListLine.Builder builder) {
    _action = requireNonNull(builder._action);
    _dscps = ImmutableSet.copyOf(requireNonNull(builder._dscps));
    _dstAddressGroup = builder._dstAddressGroup;
    _dstIpWildcard = requireNonNull(builder._dstIpWildcard);
    _dstPortRanges = ImmutableList.copyOf(requireNonNull(builder._dstPortRanges));
    _icmpCode = builder._icmpCode;
    _icmpType = builder._icmpType;
    _name = requireNonNull(builder._name);
    _nexthop1 = builder._nexthop1;
    _nexthop2 = builder._nexthop2;
    _nexthop3 = builder._nexthop3;
    _protocol = builder._protocol;
    _srcAddressGroup = builder._srcAddressGroup;
    _srcIpWildcard = requireNonNull(builder._srcIpWildcard);
    _srcPortRanges = ImmutableList.copyOf(requireNonNull(builder._srcPortRanges));
    _tcpFlags = ImmutableList.copyOf(requireNonNull(builder._tcpFlags));
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nonnull Ip6Wildcard getDestinationIpWildcard() {
    return _dstIpWildcard;
  }

  public @Nonnull Set<Integer> getDscps() {
    return _dscps;
  }

  public @Nullable String getDstAddressGroup() {
    return _dstAddressGroup;
  }

  public @Nonnull List<SubRange> getDstPorts() {
    return _dstPortRanges;
  }

  public @Nullable Integer getIcmpCode() {
    return _icmpCode;
  }

  public @Nullable Integer getIcmpType() {
    return _icmpType;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Ipv6Nexthop getNexthop1() {
    return _nexthop1;
  }

  public @Nullable Ipv6Nexthop getNexthop2() {
    return _nexthop2;
  }

  public @Nullable Ipv6Nexthop getNexthop3() {
    return _nexthop3;
  }

  public @Nonnull Optional<IpProtocol> getProtocol() {
    return Optional.ofNullable(_protocol);
  }

  public @Nonnull Ip6Wildcard getSourceIpWildcard() {
    return _srcIpWildcard;
  }

  public @Nullable String getSrcAddressGroup() {
    return _srcAddressGroup;
  }

  public @Nonnull List<SubRange> getSrcPorts() {
    return _srcPortRanges;
  }

  public @Nonnull List<TcpFlagsMatchConditions> getTcpFlags() {
    return _tcpFlags;
  }

  @Override
  public String toString() {
    return "[Name:\""
        + _name
        + "\", Action:"
        + _action
        + ", Protocol:"
        + (_protocol == null ? "any" : _protocol)
        + ", SourceIpWildcard:"
        + _srcIpWildcard
        + ", DestinationIpWildcard:"
        + _dstIpWildcard
        + ", PortRange:"
        + _srcPortRanges
        + "]";
  }
}
