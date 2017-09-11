package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.State;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;

public class ExtendedAccessListLine implements Serializable {

  private static final long serialVersionUID = 1L;

  private final LineAction _action;

  private final Set<Integer> _dscps;

  private final String _dstAddressGroup;

  private final IpWildcard _dstIpWildcard;

  private final List<SubRange> _dstPortRanges;

  private final Set<Integer> _ecns;

  private final Integer _icmpCode;

  private final Integer _icmpType;

  private final String _name;

  private final IpProtocol _protocol;

  private final String _srcAddressGroup;

  private final IpWildcard _srcIpWildcard;

  private final List<SubRange> _srcPortRanges;

  private Set<State> _states;

  private final List<TcpFlags> _tcpFlags;

  public ExtendedAccessListLine(
      String name,
      LineAction action,
      IpProtocol protocol,
      IpWildcard srcIpWildcard,
      @Nullable String srcAddressGroup,
      IpWildcard dstIpWildcard,
      @Nullable String dstAddressGroup,
      List<SubRange> srcPortRanges,
      List<SubRange> dstPortRanges,
      Set<Integer> dscps,
      Set<Integer> ecns,
      @Nullable Integer icmpType,
      @Nullable Integer icmpCode,
      Set<State> states,
      List<TcpFlags> tcpFlags) {
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

  public IpWildcard getDestinationIpWildcard() {
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

  public IpProtocol getProtocol() {
    return _protocol;
  }

  public IpWildcard getSourceIpWildcard() {
    return _srcIpWildcard;
  }

  public String getSrcAddressGroup() {
    return _srcAddressGroup;
  }

  public List<SubRange> getSrcPorts() {
    return _srcPortRanges;
  }

  public Set<State> getStates() {
    return _states;
  }

  public List<TcpFlags> getTcpFlags() {
    return _tcpFlags;
  }

  @Override
  public String toString() {
    String protocolName = _protocol.name();
    return "[Name:\""
        + _name
        + "\", Action:"
        + _action
        + ", Protocol:"
        + protocolName
        + "("
        + _protocol.number()
        + ")"
        + ", SourceIpWildcard:"
        + _srcIpWildcard
        + ", DestinationIpWildcard:"
        + _dstIpWildcard
        + ", PortRange:"
        + _srcPortRanges
        + "]";
  }
}
