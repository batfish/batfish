package org.batfish.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.batfish.util.SubRange;

public final class IpAccessListLine implements Serializable {

   private static final long serialVersionUID = 1L;

   private LineAction _action;

   private Set<Prefix> _dstIpRanges;

   private List<SubRange> _dstPortRanges;

   private int _icmpCode;

   private int _icmpType;

   private String _invalidMessage;

   private Set<IpProtocol> _protocols;

   private Set<Prefix> _srcIpRanges;

   private List<SubRange> _srcPortRanges;

   private int _tcpFlags;

   public IpAccessListLine() {
      _protocols = EnumSet.noneOf(IpProtocol.class);
      _dstIpRanges = new TreeSet<Prefix>();
      _dstPortRanges = new ArrayList<SubRange>();
      _srcIpRanges = new TreeSet<Prefix>();
      _srcPortRanges = new ArrayList<SubRange>();
      _icmpType = IcmpType.UNSET;
      _icmpCode = IcmpCode.UNSET;
      _tcpFlags = TcpFlags.UNSET;
   }

   public IpAccessListLine copy() {
      IpAccessListLine line = new IpAccessListLine();
      line._action = _action;
      line._dstIpRanges.addAll(_dstIpRanges);
      line._dstPortRanges.addAll(_dstPortRanges);
      line._icmpCode = _icmpCode;
      line._icmpType = _icmpType;
      line._invalidMessage = _invalidMessage;
      line._protocols.addAll(_protocols);
      line._srcIpRanges.addAll(_srcIpRanges);
      line._srcPortRanges.addAll(_srcPortRanges);
      line._tcpFlags = _tcpFlags;
      return line;
   }

   public LineAction getAction() {
      return _action;
   }

   public Set<Prefix> getDestinationIpRanges() {
      return _dstIpRanges;
   }

   public List<SubRange> getDstPortRanges() {
      return _dstPortRanges;
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

   public List<SubRange> getSrcPortRanges() {
      return _srcPortRanges;
   }

   public int getTcpFlags() {
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

   public void setTcpFlags(int tcpFlags) {
      _tcpFlags = tcpFlags;
   }

   @Override
   public String toString() {
      return "[Action:" + _action + ", Protocols:" + _protocols.toString()
            + ", SourceIpRanges:" + _srcIpRanges + ", DestinationIpRanges:"
            + _dstIpRanges + ", SrcPortRanges:" + _srcPortRanges
            + ", DstPortRanges:" + _dstPortRanges + ", IcmpType:" + _icmpType
            + ", IcmpCode:" + _icmpCode + ", TcpFlags:" + _tcpFlags + "]";
   }

}
