package org.batfish.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.common.util.SubRange;
import org.batfish.datamodel.IcmpCode;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;

public final class IpAccessListLine implements Serializable {

   private static final long serialVersionUID = 1L;

   private LineAction _action;

   private final Set<Integer> _dscps;

   private final Set<Prefix> _dstIpRanges;

   private final List<SubRange> _dstPortRanges;

   private final Set<Integer> _ecns;

   private int _icmpCode;

   private int _icmpType;

   private String _invalidMessage;

   private final Set<IpProtocol> _protocols;

   private final Set<Prefix> _srcIpRanges;

   private final Set<Prefix> _srcOrDstIpRanges;

   private final List<SubRange> _srcOrDstPortRanges;

   private final List<SubRange> _srcPortRanges;

   private final List<TcpFlags> _tcpFlags;

   public IpAccessListLine() {
      _dscps = new TreeSet<Integer>();
      _protocols = EnumSet.noneOf(IpProtocol.class);
      _dstIpRanges = new TreeSet<Prefix>();
      _dstPortRanges = new ArrayList<SubRange>();
      _ecns = new TreeSet<Integer>();
      _srcIpRanges = new TreeSet<Prefix>();
      _srcOrDstIpRanges = new TreeSet<Prefix>();
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
      line._dstIpRanges.addAll(_dstIpRanges);
      line._dstPortRanges.addAll(_dstPortRanges);
      line._ecns.addAll(_ecns);
      line._icmpCode = _icmpCode;
      line._icmpType = _icmpType;
      line._invalidMessage = _invalidMessage;
      line._protocols.addAll(_protocols);
      line._srcIpRanges.addAll(_srcIpRanges);
      line._srcOrDstIpRanges.addAll(_srcOrDstIpRanges);
      line._srcOrDstPortRanges.addAll(_srcOrDstPortRanges);
      line._srcPortRanges.addAll(_srcPortRanges);
      line._tcpFlags.addAll(_tcpFlags);
      return line;
   }

   public LineAction getAction() {
      return _action;
   }

   public Set<Prefix> getDestinationIpRanges() {
      return _dstIpRanges;
   }

   public Set<Integer> getDscps() {
      return _dscps;
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

   public Set<Prefix> getSourceIpRanges() {
      return _srcIpRanges;
   }

   public Set<Prefix> getSrcOrDstIpRanges() {
      return _srcOrDstIpRanges;
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
            + ", SourceIpRanges:" + _srcIpRanges + ", DestinationIpRanges:"
            + _dstIpRanges + ", SrcOrDstIpRanges:" + _srcOrDstIpRanges
            + ", SrcPortRanges:" + _srcPortRanges + ", DstPortRanges:"
            + _dstPortRanges + ", SrcOrDstPortRanges:" + _srcOrDstPortRanges
            + ", Dscps: " + _dscps.toString() + ", IcmpType:" + _icmpType
            + ", IcmpCode:" + _icmpCode + ", TcpFlags:" + _tcpFlags.toString()
            + "]";
   }

}
