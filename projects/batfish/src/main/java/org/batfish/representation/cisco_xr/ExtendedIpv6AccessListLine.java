package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.datamodel.FlowState;
import org.batfish.datamodel.Ip6Wildcard;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlagsMatchConditions;

public class ExtendedIpv6AccessListLine implements Serializable {

  private final LineAction _action;

  private final Set<Integer> _dscps;

  private final String _dstAddressGroup;

  private final Ip6Wildcard _dstIpWildcard;

  private final List<SubRange> _dstPortRanges;

  private final Set<Integer> _ecns;

  private final Integer _icmpCode;

  private final Integer _icmpType;

  private final String _name;

  private final @Nullable IpProtocol _protocol;

  private final String _srcAddressGroup;

  private final Ip6Wildcard _srcIpWildcard;

  private final List<SubRange> _srcPortRanges;

  private Set<FlowState> _states;

  private final List<TcpFlagsMatchConditions> _tcpFlags;

  public ExtendedIpv6AccessListLine(
      String name,
      LineAction action,
      @Nullable IpProtocol protocol,
      Ip6Wildcard srcIpWildcard,
      @Nullable String srcAddressGroup,
      Ip6Wildcard dstIpWildcard,
      @Nullable String dstAddressGroup,
      List<SubRange> srcPortRanges,
      List<SubRange> dstPortRanges,
      Set<Integer> dscps,
      Set<Integer> ecns,
      @Nullable Integer icmpType,
      @Nullable Integer icmpCode,
      Set<FlowState> states,
      List<TcpFlagsMatchConditions> tcpFlags) {
    _name = name;
    _action = action;
    _protocol = protocol;
    _srcIpWildcard = srcIpWildcard;
    _srcAddressGroup = srcAddressGroup;
    _dscps = dscps;
    _dstIpWildcard = dstIpWildcard;
    _dstAddressGroup = dstAddressGroup;
    _ecns = ecns;
    _srcPortRanges = srcPortRanges;
    _dstPortRanges = dstPortRanges;
    _icmpType = icmpType;
    _icmpCode = icmpCode;
    _states = states;
    _tcpFlags = tcpFlags;
  }

  public LineAction getAction() {
    return _action;
  }

  public Ip6Wildcard getDestinationIpWildcard() {
    return _dstIpWildcard;
  }

  public Set<Integer> getDscps() {
    return _dscps;
  }

  public String getDstAddressGroup() {
    return _dstAddressGroup;
  }

  public List<SubRange> getDstPorts() {
    return _dstPortRanges;
  }

  public Set<Integer> getEcns() {
    return _ecns;
  }

  public Integer getIcmpCode() {
    return _icmpCode;
  }

  public Integer getIcmpType() {
    return _icmpType;
  }

  public String getName() {
    return _name;
  }

  public Optional<IpProtocol> getProtocol() {
    return Optional.ofNullable(_protocol);
  }

  public Ip6Wildcard getSourceIpWildcard() {
    return _srcIpWildcard;
  }

  public String getSrcAddressGroup() {
    return _srcAddressGroup;
  }

  public List<SubRange> getSrcPorts() {
    return _srcPortRanges;
  }

  public Set<FlowState> getStates() {
    return _states;
  }

  public List<TcpFlagsMatchConditions> getTcpFlags() {
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
