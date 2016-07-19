package org.batfish.datamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class HeaderSpace implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Set<Integer> _dscps;

   private Set<IpWildcard> _dstIpWildcards;

   private List<SubRange> _dstPortRanges;

   private Set<Integer> _ecns;

   private int _icmpCode;

   private int _icmpType;

   private Set<IpProtocol> _protocols;

   private Set<IpWildcard> _srcIpWildcards;

   private Set<IpWildcard> _srcOrDstIpWildcards;

   private List<SubRange> _srcOrDstPortRanges;

   private List<SubRange> _srcPortRanges;

   private Set<State> _states;

   private List<TcpFlags> _tcpFlags;

   public HeaderSpace() {
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
      _states = EnumSet.noneOf(State.class);
      _tcpFlags = new ArrayList<TcpFlags>();
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

   public Set<State> getStates() {
      return _states;
   }

   public List<TcpFlags> getTcpFlags() {
      return _tcpFlags;
   }

   public void setDscps(Set<Integer> dscps) {
      _dscps = dscps;
   }

   public void setDstIpWildcards(Set<IpWildcard> dstIpWildcards) {
      _dstIpWildcards = dstIpWildcards;
   }

   public void setDstPortRanges(List<SubRange> dstPortRanges) {
      _dstPortRanges = dstPortRanges;
   }

   public void setEcns(Set<Integer> ecns) {
      _ecns = ecns;
   }

   public void setIcmpCode(int icmpCode) {
      _icmpCode = icmpCode;
   }

   public void setIcmpType(int icmpType) {
      _icmpType = icmpType;
   }

   public void setProtocols(Set<IpProtocol> protocols) {
      _protocols = protocols;
   }

   public void setSrcIpWildcards(Set<IpWildcard> srcIpWildcards) {
      _srcIpWildcards = srcIpWildcards;
   }

   public void setSrcOrDstIpWildcards(Set<IpWildcard> srcOrDstIpWildcards) {
      _srcOrDstIpWildcards = srcOrDstIpWildcards;
   }

   public void setSrcOrDstPortRanges(List<SubRange> srcOrDstPortRanges) {
      _srcOrDstPortRanges = srcOrDstPortRanges;
   }

   public void setSrcPortRanges(List<SubRange> srcPortRanges) {
      _srcPortRanges = srcPortRanges;
   }

   public void setStates(Set<State> states) {
      _states = states;
   }

   public void setTcpFlags(List<TcpFlags> tcpFlags) {
      _tcpFlags = tcpFlags;
   }

   @Override
   public String toString() {
      return "[Protocols:" + _protocols.toString() + ", SourceIpWildcards:"
            + _srcIpWildcards + ", DestinationIpWildcards:" + _dstIpWildcards
            + ", SrcOrDstIpWildcards:" + _srcOrDstIpWildcards
            + ", SrcPortRanges:" + _srcPortRanges + ", DstPortRanges:"
            + _dstPortRanges + ", SrcOrDstPortRanges:" + _srcOrDstPortRanges
            + ", Dscps: " + _dscps.toString() + ", IcmpType:" + _icmpType
            + ", IcmpCode:" + _icmpCode + ", States:" + _states.toString()
            + ", TcpFlags:" + _tcpFlags.toString() + "]";
   }

}
