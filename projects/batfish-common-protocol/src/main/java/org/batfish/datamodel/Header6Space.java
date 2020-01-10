package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Header6Space implements Serializable {
  private static final String PROP_DSCPS = "dscps";
  private static final String PROP_DST_IPS = "dstIps";
  private static final String PROP_DST_PORTS = "dstPorts";
  private static final String PROP_ECNS = "ecns";
  private static final String PROP_FRAGMENT_OFFSETS = "fragmentOffsets";
  private static final String PROP_ICMP_CODES = "icmpCodes";
  private static final String PROP_ICMP_TYPES = "icmpTypes";
  private static final String PROP_IP_PROTOCOLS = "ipProtocols";
  private static final String PROP_NEGATE = "negate";
  private static final String PROP_NOT_DSCPS = "notDscps";
  private static final String PROP_NOT_DST_IPS = "notDstIps";
  private static final String PROP_NOT_DST_PORTS = "notDstPorts";
  private static final String PROP_NOT_ECNS = "notEcns";
  private static final String PROP_NOT_FRAGMENT_OFFSETS = "notFragmentOffsets";
  private static final String PROP_NOT_ICMP_CODES = "notIcmpCodes";
  private static final String PROP_NOT_ICMP_TYPES = "notIcmpTypes";
  private static final String PROP_NOT_IP_PROTOCOLS = "notIpProtocols";
  private static final String PROP_NOT_SRC_IPS = "notSrcIps";
  private static final String PROP_NOT_SRC_PORTS = "notSrcPorts";
  private static final String PROP_SRC_IPS = "srcIps";
  private static final String PROP_SRC_OR_DST_IPS = "srcOrDstIps";
  private static final String PROP_SRC_OR_DST_PORTS = "srcOrDstPorts";
  private static final String PROP_SRC_PORTS = "srcPorts";
  private static final String PROP_DEPRECATED_STATES = "states";
  private static final String PROP_TCP_FLAGS_MATCH_CONDITIONS = "tcpFlagsMatchConditions";

  private static boolean rangesContain(Collection<SubRange> ranges, int num) {
    for (SubRange range : ranges) {
      if (range.getStart() <= num && num <= range.getEnd()) {
        return true;
      }
    }
    return false;
  }

  private static boolean wildcardsContain(Collection<Ip6Wildcard> wildcards, Ip6 ip) {
    for (Ip6Wildcard wildcard : wildcards) {
      if (wildcard.contains(ip)) {
        return true;
      }
    }
    return false;
  }

  private SortedSet<Integer> _dscps;

  private SortedSet<Ip6Wildcard> _dstIps;

  private SortedSet<SubRange> _dstPorts;

  private SortedSet<Integer> _ecns;

  private SortedSet<SubRange> _fragmentOffsets;

  private SortedSet<SubRange> _icmpCodes;

  private SortedSet<SubRange> _icmpTypes;

  private Set<IpProtocol> _ipProtocols;

  private boolean _negate;

  private SortedSet<Integer> _notDscps;

  private SortedSet<Ip6Wildcard> _notDstIps;

  private SortedSet<SubRange> _notDstPorts;

  private SortedSet<Integer> _notEcns;

  private SortedSet<SubRange> _notFragmentOffsets;

  private SortedSet<SubRange> _notIcmpCodes;

  private SortedSet<SubRange> _notIcmpTypes;

  private Set<IpProtocol> _notIpProtocols;

  private SortedSet<Ip6Wildcard> _notSrcIps;

  private SortedSet<SubRange> _notSrcPorts;

  private SortedSet<Ip6Wildcard> _srcIps;

  private SortedSet<Ip6Wildcard> _srcOrDstIps;

  private SortedSet<SubRange> _srcOrDstPorts;

  private SortedSet<SubRange> _srcPorts;

  private List<TcpFlagsMatchConditions> _tcpFlags;

  public Header6Space() {
    _dscps = new TreeSet<>();
    _dstIps = new TreeSet<>();
    _dstPorts = new TreeSet<>();
    _ecns = new TreeSet<>();
    _fragmentOffsets = new TreeSet<>();
    _ipProtocols = EnumSet.noneOf(IpProtocol.class);
    _srcIps = new TreeSet<>();
    _srcOrDstIps = new TreeSet<>();
    _srcOrDstPorts = new TreeSet<>();
    _srcPorts = new TreeSet<>();
    _icmpTypes = new TreeSet<>();
    _icmpCodes = new TreeSet<>();
    _tcpFlags = new ArrayList<>();
    _notDscps = new TreeSet<>();
    _notDstIps = new TreeSet<>();
    _notDstPorts = new TreeSet<>();
    _notEcns = new TreeSet<>();
    _notFragmentOffsets = new TreeSet<>();
    _notIcmpCodes = new TreeSet<>();
    _notIcmpTypes = new TreeSet<>();
    _notIpProtocols = EnumSet.noneOf(IpProtocol.class);
    _notSrcIps = new TreeSet<>();
    _notSrcPorts = new TreeSet<>();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof Header6Space)) {
      return false;
    }
    Header6Space other = (Header6Space) o;
    if (!_dscps.equals(other._dscps)) {
      return false;
    }
    if (!_dstIps.equals(other._dstIps)) {
      return false;
    }
    if (!_dstPorts.equals(other._dstPorts)) {
      return false;
    }
    if (!_ecns.equals(other._ecns)) {
      return false;
    }
    if (!_fragmentOffsets.equals(other._fragmentOffsets)) {
      return false;
    }
    if (!_icmpCodes.equals(other._icmpCodes)) {
      return false;
    }
    if (!_icmpTypes.equals(other._icmpTypes)) {
      return false;
    }
    if (!_ipProtocols.equals(other._ipProtocols)) {
      return false;
    }
    if (_negate != other._negate) {
      return false;
    }
    if (!_notDscps.equals(other._notDscps)) {
      return false;
    }
    if (!_notDstIps.equals(other._notDstIps)) {
      return false;
    }
    if (!_notDstPorts.equals(other._notDstPorts)) {
      return false;
    }
    if (!_notEcns.equals(other._notEcns)) {
      return false;
    }
    if (!_notFragmentOffsets.equals(other._notFragmentOffsets)) {
      return false;
    }
    if (!_notIcmpCodes.equals(other._notIcmpCodes)) {
      return false;
    }
    if (!_notIcmpTypes.equals(other._notIcmpTypes)) {
      return false;
    }
    if (!_notIpProtocols.equals(other._notIpProtocols)) {
      return false;
    }
    if (!_notSrcIps.equals(other._notSrcIps)) {
      return false;
    }
    if (!_notSrcPorts.equals(other._notSrcPorts)) {
      return false;
    }
    if (!_srcIps.equals(other._srcIps)) {
      return false;
    }
    if (!_srcOrDstIps.equals(other._srcOrDstIps)) {
      return false;
    }
    if (!_srcOrDstPorts.equals(other._srcOrDstPorts)) {
      return false;
    }
    if (!_srcPorts.equals(other._srcPorts)) {
      return false;
    }
    if (!_tcpFlags.equals(other._tcpFlags)) {
      return false;
    }
    return true;
  }

  @JsonProperty(PROP_DSCPS)
  public SortedSet<Integer> getDscps() {
    return _dscps;
  }

  @JsonProperty(PROP_DST_IPS)
  public SortedSet<Ip6Wildcard> getDstIps() {
    return _dstIps;
  }

  @JsonProperty(PROP_DST_PORTS)
  public SortedSet<SubRange> getDstPorts() {
    return _dstPorts;
  }

  @JsonProperty(PROP_ECNS)
  public SortedSet<Integer> getEcns() {
    return _ecns;
  }

  @JsonProperty(PROP_FRAGMENT_OFFSETS)
  public SortedSet<SubRange> getFragmentOffsets() {
    return _fragmentOffsets;
  }

  @JsonProperty(PROP_ICMP_CODES)
  public SortedSet<SubRange> getIcmpCodes() {
    return _icmpCodes;
  }

  @JsonProperty(PROP_ICMP_TYPES)
  public SortedSet<SubRange> getIcmpTypes() {
    return _icmpTypes;
  }

  @JsonProperty(PROP_IP_PROTOCOLS)
  public Set<IpProtocol> getIpProtocols() {
    return _ipProtocols;
  }

  @JsonProperty(PROP_NEGATE)
  public boolean getNegate() {
    return _negate;
  }

  @JsonProperty(PROP_NOT_DSCPS)
  public SortedSet<Integer> getNotDscps() {
    return _notDscps;
  }

  @JsonProperty(PROP_NOT_DST_IPS)
  public SortedSet<Ip6Wildcard> getNotDstIps() {
    return _notDstIps;
  }

  @JsonProperty(PROP_NOT_DST_PORTS)
  public SortedSet<SubRange> getNotDstPorts() {
    return _notDstPorts;
  }

  @JsonProperty(PROP_NOT_ECNS)
  public SortedSet<Integer> getNotEcns() {
    return _notEcns;
  }

  @JsonProperty(PROP_NOT_FRAGMENT_OFFSETS)
  public SortedSet<SubRange> getNotFragmentOffsets() {
    return _notFragmentOffsets;
  }

  @JsonProperty(PROP_NOT_ICMP_CODES)
  public SortedSet<SubRange> getNotIcmpCodes() {
    return _notIcmpCodes;
  }

  @JsonProperty(PROP_NOT_ICMP_TYPES)
  public SortedSet<SubRange> getNotIcmpTypes() {
    return _notIcmpTypes;
  }

  @JsonProperty(PROP_NOT_IP_PROTOCOLS)
  public Set<IpProtocol> getNotIpProtocols() {
    return _notIpProtocols;
  }

  @JsonProperty(PROP_NOT_SRC_IPS)
  public SortedSet<Ip6Wildcard> getNotSrcIps() {
    return _notSrcIps;
  }

  @JsonProperty(PROP_NOT_SRC_PORTS)
  public SortedSet<SubRange> getNotSrcPorts() {
    return _notSrcPorts;
  }

  @JsonProperty(PROP_SRC_IPS)
  public SortedSet<Ip6Wildcard> getSrcIps() {
    return _srcIps;
  }

  @JsonProperty(PROP_SRC_OR_DST_IPS)
  public SortedSet<Ip6Wildcard> getSrcOrDstIps() {
    return _srcOrDstIps;
  }

  @JsonProperty(PROP_SRC_OR_DST_PORTS)
  public SortedSet<SubRange> getSrcOrDstPorts() {
    return _srcOrDstPorts;
  }

  @JsonProperty(PROP_SRC_PORTS)
  public SortedSet<SubRange> getSrcPorts() {
    return _srcPorts;
  }

  @JsonProperty(PROP_TCP_FLAGS_MATCH_CONDITIONS)
  public List<TcpFlagsMatchConditions> getTcpFlags() {
    return _tcpFlags;
  }

  @Override
  public int hashCode() {
    // TODO: implement better hashcode
    return 0;
  }

  public boolean matches(Flow6 flow) {
    if (!_dscps.isEmpty() && !_dscps.contains(flow.getDscp())) {
      return false;
    }
    if (!_notDscps.isEmpty() && _notDscps.contains(flow.getDscp())) {
      return false;
    }
    if (!_dstIps.isEmpty() && !wildcardsContain(_dstIps, flow.getDstIp())) {
      return false;
    }
    if (!_notDstIps.isEmpty() && wildcardsContain(_notDstIps, flow.getDstIp())) {
      return false;
    }
    if (!_dstPorts.isEmpty() && !rangesContain(_dstPorts, flow.getDstPort())) {
      return false;
    }
    if (!_notDstPorts.isEmpty() && rangesContain(_notDstPorts, flow.getDstPort())) {
      return false;
    }
    if (!_fragmentOffsets.isEmpty() && !rangesContain(_fragmentOffsets, flow.getFragmentOffset())) {
      return false;
    }
    if (!_notFragmentOffsets.isEmpty()
        && rangesContain(_notFragmentOffsets, flow.getFragmentOffset())) {
      return false;
    }
    if (!_icmpCodes.isEmpty() && !rangesContain(_icmpCodes, flow.getIcmpCode())) {
      return false;
    }
    if (!_notIcmpCodes.isEmpty() && rangesContain(_notIcmpCodes, flow.getFragmentOffset())) {
      return false;
    }
    if (!_icmpTypes.isEmpty() && !rangesContain(_icmpTypes, flow.getIcmpType())) {
      return false;
    }
    if (!_notIcmpTypes.isEmpty() && rangesContain(_notIcmpTypes, flow.getFragmentOffset())) {
      return false;
    }
    if (!_ipProtocols.isEmpty() && !_ipProtocols.contains(flow.getIpProtocol())) {
      return false;
    }
    if (!_notIpProtocols.isEmpty() && _notIpProtocols.contains(flow.getIpProtocol())) {
      return false;
    }
    if (!_srcOrDstIps.isEmpty()
        && !(wildcardsContain(_srcOrDstIps, flow.getSrcIp())
            || wildcardsContain(_srcOrDstIps, flow.getDstIp()))) {
      return false;
    }
    if (!_srcOrDstPorts.isEmpty()
        && !(rangesContain(_srcOrDstPorts, flow.getSrcPort())
            || rangesContain(_srcOrDstPorts, flow.getDstPort()))) {
      return false;
    }
    if (!_srcIps.isEmpty() && !wildcardsContain(_srcIps, flow.getSrcIp())) {
      return false;
    }
    if (!_notSrcIps.isEmpty() && wildcardsContain(_notSrcIps, flow.getSrcIp())) {
      return false;
    }
    if (!_srcPorts.isEmpty() && !rangesContain(_srcPorts, flow.getSrcPort())) {
      return false;
    }
    if (!_notSrcPorts.isEmpty() && rangesContain(_notSrcPorts, flow.getSrcPort())) {
      return false;
    }
    if (!_tcpFlags.isEmpty() && _tcpFlags.stream().noneMatch(tcpFlags -> tcpFlags.match(flow))) {
      return false;
    }
    return true;
  }

  @JsonProperty(PROP_DSCPS)
  public void setDscps(SortedSet<Integer> dscps) {
    _dscps = dscps;
  }

  @JsonProperty(PROP_DST_IPS)
  public void setDstIps(SortedSet<Ip6Wildcard> dstIps) {
    _dstIps = dstIps;
  }

  @JsonProperty(PROP_DST_PORTS)
  public void setDstPorts(SortedSet<SubRange> dstPorts) {
    _dstPorts = dstPorts;
  }

  @JsonProperty(PROP_ECNS)
  public void setEcns(SortedSet<Integer> ecns) {
    _ecns = ecns;
  }

  @JsonProperty(PROP_FRAGMENT_OFFSETS)
  public void setFragmentOffsets(SortedSet<SubRange> fragmentOffsets) {
    _fragmentOffsets = fragmentOffsets;
  }

  @JsonProperty(PROP_ICMP_CODES)
  public void setIcmpCodes(SortedSet<SubRange> icmpCodes) {
    _icmpCodes = icmpCodes;
  }

  @JsonProperty(PROP_ICMP_TYPES)
  public void setIcmpTypes(SortedSet<SubRange> icmpTypes) {
    _icmpTypes = icmpTypes;
  }

  @JsonProperty(PROP_IP_PROTOCOLS)
  public void setIpProtocols(Set<IpProtocol> ipProtocols) {
    _ipProtocols.clear();
    _ipProtocols.addAll(ipProtocols);
  }

  @JsonProperty(PROP_NEGATE)
  public void setNegate(boolean negate) {
    _negate = negate;
  }

  @JsonProperty(PROP_NOT_DSCPS)
  public void setNotDscps(SortedSet<Integer> notDscps) {
    _notDscps = notDscps;
  }

  @JsonProperty(PROP_NOT_DST_IPS)
  public void setNotDstIps(SortedSet<Ip6Wildcard> notDstIps) {
    _notDstIps = notDstIps;
  }

  @JsonProperty(PROP_NOT_DST_PORTS)
  public void setNotDstPorts(SortedSet<SubRange> notDstPorts) {
    _notDstPorts = notDstPorts;
  }

  @JsonProperty(PROP_NOT_ECNS)
  public void setNotEcns(SortedSet<Integer> notEcns) {
    _notEcns = notEcns;
  }

  @JsonProperty(PROP_NOT_FRAGMENT_OFFSETS)
  public void setNotFragmentOffsets(SortedSet<SubRange> notFragmentOffsets) {
    _notFragmentOffsets = notFragmentOffsets;
  }

  @JsonProperty(PROP_NOT_ICMP_CODES)
  public void setNotIcmpCodes(SortedSet<SubRange> notIcmpCodes) {
    _notIcmpCodes = notIcmpCodes;
  }

  @JsonProperty(PROP_NOT_ICMP_TYPES)
  public void setNotIcmpTypes(SortedSet<SubRange> notIcmpTypes) {
    _notIcmpTypes = notIcmpTypes;
  }

  @JsonProperty(PROP_NOT_IP_PROTOCOLS)
  public void setNotIpProtocols(Set<IpProtocol> notIpProtocols) {
    _notIpProtocols.clear();
    _notIpProtocols.addAll(notIpProtocols);
  }

  @JsonProperty(PROP_NOT_SRC_IPS)
  public void setNotSrcIps(SortedSet<Ip6Wildcard> notSrcIps) {
    _notSrcIps = notSrcIps;
  }

  @JsonProperty(PROP_NOT_SRC_PORTS)
  public void setNotSrcPorts(SortedSet<SubRange> notSrcPorts) {
    _notSrcPorts = notSrcPorts;
  }

  @JsonProperty(PROP_SRC_IPS)
  public void setSrcIps(SortedSet<Ip6Wildcard> srcIps) {
    _srcIps = srcIps;
  }

  @JsonProperty(PROP_SRC_OR_DST_IPS)
  public void setSrcOrDstIps(SortedSet<Ip6Wildcard> srcOrDstIps) {
    _srcOrDstIps = srcOrDstIps;
  }

  @JsonProperty(PROP_SRC_OR_DST_PORTS)
  public void setSrcOrDstPorts(SortedSet<SubRange> srcOrDstPorts) {
    _srcOrDstPorts = srcOrDstPorts;
  }

  @JsonProperty(PROP_SRC_PORTS)
  public void setSrcPorts(SortedSet<SubRange> srcPorts) {
    _srcPorts = srcPorts;
  }

  @JsonProperty(PROP_DEPRECATED_STATES)
  @Deprecated
  private void setStates(Object ignored) {}

  @JsonProperty(PROP_TCP_FLAGS_MATCH_CONDITIONS)
  public void setTcpFlags(List<TcpFlagsMatchConditions> tcpFlags) {
    _tcpFlags = tcpFlags;
  }

  @Override
  public String toString() {
    return "[Protocols:"
        + _ipProtocols
        + ", SrcIps:"
        + _srcIps
        + ", NotSrcIps:"
        + _notSrcIps
        + ", DstIps:"
        + _dstIps
        + ", NotDstIps:"
        + _notDstIps
        + ", SrcOrDstIps:"
        + _srcOrDstIps
        + ", SrcPorts:"
        + _srcPorts
        + ", NotSrcPorts:"
        + _notSrcPorts
        + ", DstPorts:"
        + _dstPorts
        + ", NotDstPorts:"
        + _notDstPorts
        + ", SrcOrDstPorts:"
        + _srcOrDstPorts
        + ", Dscps: "
        + _dscps
        + ", NotDscps: "
        + _notDscps
        + ", Ecns: "
        + _ecns
        + ", NotEcns: "
        + _notEcns
        + ", FragmentOffsets: "
        + _fragmentOffsets
        + ", NotFragmentOffsets: "
        + _notFragmentOffsets
        + ", IcmpType:"
        + _icmpTypes
        + ", NotIcmpType:"
        + _notIcmpTypes
        + ", IcmpCode:"
        + _icmpCodes
        + ", NotIcmpCode:"
        + _notIcmpCodes
        + ", TcpFlagsMatchConditions:"
        + _tcpFlags
        + "]";
  }
}
