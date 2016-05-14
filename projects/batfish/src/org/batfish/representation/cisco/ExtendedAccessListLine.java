package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;

public class ExtendedAccessListLine implements Serializable {

   private static final long serialVersionUID = 1L;

   private LineAction _action;

   private Set<Integer> _dscps;

   private String _dstAddressGroup;

   private IpWildcard _dstIpWildcard;

   private List<SubRange> _dstPortRanges;

   private Set<Integer> _ecns;

   private Integer _icmpCode;

   private Integer _icmpType;

   private IpProtocol _protocol;

   private String _srcAddressGroup;

   private IpWildcard _srcIpWildcard;

   private List<SubRange> _srcPortRanges;

   private List<TcpFlags> _tcpFlags;

   public ExtendedAccessListLine(LineAction action, IpProtocol protocol,
         IpWildcard srcIpWildcard, String srcAddressGroup,
         IpWildcard dstIpWildcard, String dstAddressGroup,
         List<SubRange> srcPortRanges, List<SubRange> dstPortRanges,
         Set<Integer> dscps, Set<Integer> ecns, Integer icmpType,
         Integer icmpCode, List<TcpFlags> tcpFlags) {
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

   public IpWildcard getSourceIpWildcard() {
      return _srcIpWildcard;
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
            + _protocol.number() + ")" + ", SourceIpWildcard:" + _srcIpWildcard
            + ", DestinationIpWildcard:" + _dstIpWildcard + ", PortRange:"
            + _srcPortRanges + "]";
   }

}
