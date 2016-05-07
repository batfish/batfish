package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.batfish.representation.Ip;
import org.batfish.representation.IpProtocol;
import org.batfish.representation.LineAction;
import org.batfish.representation.TcpFlags;
import org.batfish.util.SubRange;

public class ExtendedAccessListLine implements Serializable {

   private static final long serialVersionUID = 1L;

   private LineAction _action;

   private Set<Integer> _dscps;

   private String _dstAddressGroup;

   private Ip _dstIp;

   private List<SubRange> _dstPortRanges;

   private Ip _dstWildcard;

   private Set<Integer> _ecns;

   private Integer _icmpCode;

   private Integer _icmpType;

   private IpProtocol _protocol;

   private String _srcAddressGroup;

   private Ip _srcIp;

   private List<SubRange> _srcPortRanges;

   private Ip _srcWildcard;

   private List<TcpFlags> _tcpFlags;

   public ExtendedAccessListLine(LineAction action, IpProtocol protocol,
         Ip srcIp, Ip srcWildcard, String srcAddressGroup, Ip dstIp,
         Ip dstWildcard, String dstAddressGroup, List<SubRange> srcPortRanges,
         List<SubRange> dstPortRanges, Set<Integer> dscps, Set<Integer> ecns,
         Integer icmpType, Integer icmpCode, List<TcpFlags> tcpFlags) {
      _action = action;
      _protocol = protocol;
      _srcIp = srcIp;
      _srcWildcard = srcWildcard;
      _srcAddressGroup = srcAddressGroup;
      _dscps = dscps;
      _dstIp = dstIp;
      _dstWildcard = dstWildcard;
      _dstAddressGroup = dstAddressGroup;
      _ecns = ecns;
      _srcPortRanges = srcPortRanges;
      _dstPortRanges = dstPortRanges;
      _icmpType = icmpType;
      _icmpCode = icmpCode;
      _tcpFlags = tcpFlags;
   }

   public LineAction getAction() {
      return _action;
   }

   public Ip getDestinationIp() {
      return _dstIp;
   }

   public Ip getDestinationWildcard() {
      return _dstWildcard;
   }

   public Set<Integer> getDscps() {
      return _dscps;
   }

   public String getDstAddressGroup() {
      return _dstAddressGroup;
   }

   public List<SubRange> getDstPortRanges() {
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

   public IpProtocol getProtocol() {
      return _protocol;
   }

   public Ip getSourceIp() {
      return _srcIp;
   }

   public Ip getSourceWildcard() {
      return _srcWildcard;
   }

   public String getSrcAddressGroup() {
      return _srcAddressGroup;
   }

   public List<SubRange> getSrcPortRanges() {
      return _srcPortRanges;
   }

   public List<TcpFlags> getTcpFlags() {
      return _tcpFlags;
   }

   @Override
   public String toString() {
      String protocolName = _protocol.name();
      return "[Action:" + _action + ", Protocol:" + protocolName + "("
            + _protocol.number() + ")" + ", SourceIp:" + _srcIp
            + ", SourceWildcard:" + _srcWildcard + ", DestinationIp:" + _dstIp
            + ", DestinationWildcard:" + _dstWildcard + ", PortRange:"
            + _srcPortRanges + "]";
   }

}
