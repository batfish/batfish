package org.batfish.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.datamodel.IcmpCode;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;

public final class IpAccessListLine implements Serializable {

   private static final long serialVersionUID = 1L;

   private LineAction _action;

   private final Set<Integer> _dscps;

   private final Set<IpWildcard> _dstIpWildcards;

   private final List<SubRange> _dstPortRanges;

   private final Set<Integer> _ecns;

   private int _icmpCode;

   private int _icmpType;

   private String _invalidMessage;

   private final Set<IpProtocol> _protocols;

   private final Set<IpWildcard> _srcIpWildcards;

   private final Set<IpWildcard> _srcOrDstIpWildcards;

   private final List<SubRange> _srcOrDstPortRanges;

   private final List<SubRange> _srcPortRanges;

   private final List<TcpFlags> _tcpFlags;

   public IpAccessListLine() {
      _dscps = new TreeSet<Integer>();
      _protocols = EnumSet.noneOf(IpProtocol.class);
      _dstIpWildcards = new TreeSet<IpWildcard>();
      _dstPortRanges = new ArrayList<SubRange>();
      _ecns = new TreeSet<Integer>();
      _srcIpWildcards = new TreeSet<IpWildcard>();
      _srcOrDstIpWildcards = new TreeSet<IpWildcard>();
      _srcOrDstPortRanges = new ArrayList<SubRange>();
      _srcPortRanges = new ArrayList<SubRange>();
      _icmpType = IcmpType.UNSET;
      _icmpCode = IcmpCode.UNSET;
      _tcpFlags = new ArrayList<TcpFlags>();
   }

   public IpAccessListLine copy() {
      IpAccessListLine line = new IpAccessListLine();
      line._action = _action;
      line._dscps.addAll(_dscps);
      line._dstIpWildcards.addAll(_dstIpWildcards);
      line._dstPortRanges.addAll(_dstPortRanges);
      line._ecns.addAll(_ecns);
      line._icmpCode = _icmpCode;
      line._icmpType = _icmpType;
      line._invalidMessage = _invalidMessage;
      line._protocols.addAll(_protocols);
      line._srcIpWildcards.addAll(_srcIpWildcards);
      line._srcOrDstIpWildcards.addAll(_srcOrDstIpWildcards);
      line._srcOrDstPortRanges.addAll(_srcOrDstPortRanges);
      line._srcPortRanges.addAll(_srcPortRanges);
      line._tcpFlags.addAll(_tcpFlags);
      return line;
   }

   public LineAction getAction() {
      return _action;
   }

   public Set<Integer> getDscps() {
      return _dscps;
   }

   public Set<IpWildcard> getDstIpWildcards() {
      return _dstIpWildcards;
   }

   public List<SubRange> getDstPortRanges() {
      return _dstPortRanges;
   }

   public Set<Integer> getEcns() {
      return _ecns;
   }

   public int getIcmpCode() {
      return _icmpCode;
   }

   public int getIcmpType() {
      return _icmpType;
   }

   public String getInvalidMessage() {
      return _invalidMessage;
   }

   public Set<IpProtocol> getProtocols() {
      return _protocols;
   }

   public Set<IpWildcard> getSrcIpWildcards() {
      return _srcIpWildcards;
   }

   public Set<IpWildcard> getSrcOrDstIpWildcards() {
      return _srcOrDstIpWildcards;
   }

   public List<SubRange> getSrcOrDstPortRanges() {
      return _srcOrDstPortRanges;
   }

   public List<SubRange> getSrcPortRanges() {
      return _srcPortRanges;
   }

   public List<TcpFlags> getTcpFlags() {
      return _tcpFlags;
   }

   public void setAction(LineAction action) {
      _action = action;
   }

   public void setIcmpCode(int icmpCode) {
      _icmpCode = icmpCode;
   }

   public void setIcmpType(int icmpType) {
      _icmpType = icmpType;
   }

   public void setInvalidMessage(String invalidMessage) {
      _invalidMessage = invalidMessage;
   }

   @Override
   public String toString() {
      return "[Action:" + _action + ", Protocols:" + _protocols.toString()
            + ", SourceIpWildcards:" + _srcIpWildcards
            + ", DestinationIpWildcards:" + _dstIpWildcards
            + ", SrcOrDstIpWildcards:" + _srcOrDstIpWildcards
            + ", SrcPortRanges:" + _srcPortRanges + ", DstPortRanges:"
            + _dstPortRanges + ", SrcOrDstPortRanges:" + _srcOrDstPortRanges
            + ", Dscps: " + _dscps.toString() + ", IcmpType:" + _icmpType
            + ", IcmpCode:" + _icmpCode + ", TcpFlags:" + _tcpFlags.toString()
            + "]";
   }

}
