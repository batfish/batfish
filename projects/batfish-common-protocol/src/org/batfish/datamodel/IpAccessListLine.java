package org.batfish.datamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class IpAccessListLine implements Serializable {

   private static final long serialVersionUID = 1L;

   private LineAction _action;

   private final Set<Integer> _dscps;

   private final Set<IpWildcard> _dstIpWildcards;

   private final List<SubRange> _dstPortRanges;

   private final Set<Integer> _ecns;

   private int _icmpCode;

   private int _icmpType;

   private String _name;

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

   public String getName() {
      return _name;
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

   public void setName(String name) {
      _name = name;
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
