package org.batfish.datamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class HeaderSpace implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private SortedSet<Integer> _dscps;

   private SortedSet<IpWildcard> _dstIps;

   private SortedSet<SubRange> _dstPorts;

   private SortedSet<Integer> _ecns;

   private SortedSet<SubRange> _fragmentOffsets;

   private int _icmpCode;

   private int _icmpType;

   private Set<IpProtocol> _ipProtocols;

   private boolean _negate;

   private SortedSet<Integer> _notDscps;

   private SortedSet<IpWildcard> _notDstIps;

   private SortedSet<SubRange> _notDstPorts;

   private SortedSet<Integer> _notEcns;

   private SortedSet<SubRange> _notFragmentOffsets;

   private int _notIcmpCode;

   private int _notIcmpType;

   private Set<IpProtocol> _notIpProtocols;

   private SortedSet<IpWildcard> _notSrcIps;

   private SortedSet<SubRange> _notSrcPorts;

   private SortedSet<IpWildcard> _srcIps;

   private SortedSet<IpWildcard> _srcOrDstIps;

   private SortedSet<SubRange> _srcOrDstPorts;

   private SortedSet<SubRange> _srcPorts;

   private Set<State> _states;

   private List<TcpFlags> _tcpFlags;

   public HeaderSpace() {
      _dscps = new TreeSet<Integer>();
      _dstIps = new TreeSet<IpWildcard>();
      _dstPorts = new TreeSet<SubRange>();
      _ecns = new TreeSet<Integer>();
      _fragmentOffsets = new TreeSet<SubRange>();
      _ipProtocols = EnumSet.noneOf(IpProtocol.class);
      _srcIps = new TreeSet<IpWildcard>();
      _srcOrDstIps = new TreeSet<IpWildcard>();
      _srcOrDstPorts = new TreeSet<SubRange>();
      _srcPorts = new TreeSet<SubRange>();
      _icmpType = IcmpType.UNSET;
      _icmpCode = IcmpCode.UNSET;
      _states = EnumSet.noneOf(State.class);
      _tcpFlags = new ArrayList<TcpFlags>();
      _notDscps = new TreeSet<Integer>();
      _notDstIps = new TreeSet<IpWildcard>();
      _notDstPorts = new TreeSet<SubRange>();
      _notEcns = new TreeSet<Integer>();
      _notFragmentOffsets = new TreeSet<SubRange>();
      _notIcmpCode = IcmpCode.UNSET;
      _notIcmpType = IcmpType.UNSET;
      _notIpProtocols = EnumSet.noneOf(IpProtocol.class);
      _notSrcIps = new TreeSet<IpWildcard>();
      _notSrcPorts = new TreeSet<SubRange>();
   }

   public SortedSet<Integer> getDscps() {
      return _dscps;
   }

   public SortedSet<IpWildcard> getDstIps() {
      return _dstIps;
   }

   public SortedSet<SubRange> getDstPorts() {
      return _dstPorts;
   }

   public SortedSet<Integer> getEcns() {
      return _ecns;
   }

   public SortedSet<SubRange> getFragmentOffsets() {
      return _fragmentOffsets;
   }

   public int getIcmpCode() {
      return _icmpCode;
   }

   public int getIcmpType() {
      return _icmpType;
   }

   public Set<IpProtocol> getIpProtocols() {
      return _ipProtocols;
   }

   public boolean getNegate() {
      return _negate;
   }

   public SortedSet<Integer> getNotDscps() {
      return _notDscps;
   }

   public SortedSet<IpWildcard> getNotDstIps() {
      return _notDstIps;
   }

   public SortedSet<SubRange> getNotDstPorts() {
      return _notDstPorts;
   }

   public SortedSet<Integer> getNotEcns() {
      return _notEcns;
   }

   public SortedSet<SubRange> getNotFragmentOffsets() {
      return _notFragmentOffsets;
   }

   public int getNotIcmpCode() {
      return _notIcmpCode;
   }

   public int getNotIcmpType() {
      return _notIcmpType;
   }

   public Set<IpProtocol> getNotIpProtocols() {
      return _notIpProtocols;
   }

   public SortedSet<IpWildcard> getNotSrcIps() {
      return _notSrcIps;
   }

   public SortedSet<SubRange> getNotSrcPorts() {
      return _notSrcPorts;
   }

   public SortedSet<IpWildcard> getSrcIps() {
      return _srcIps;
   }

   public SortedSet<IpWildcard> getSrcOrDstIps() {
      return _srcOrDstIps;
   }

   public SortedSet<SubRange> getSrcOrDstPorts() {
      return _srcOrDstPorts;
   }

   public SortedSet<SubRange> getSrcPorts() {
      return _srcPorts;
   }

   public Set<State> getStates() {
      return _states;
   }

   public List<TcpFlags> getTcpFlags() {
      return _tcpFlags;
   }

   public void setDscps(SortedSet<Integer> dscps) {
      _dscps = dscps;
   }

   public void setDstIps(SortedSet<IpWildcard> dstIps) {
      _dstIps = dstIps;
   }

   public void setDstPorts(SortedSet<SubRange> dstPorts) {
      _dstPorts = dstPorts;
   }

   public void setEcns(SortedSet<Integer> ecns) {
      _ecns = ecns;
   }

   public void setFragmentOffsets(SortedSet<SubRange> fragmentOffsets) {
      _fragmentOffsets = fragmentOffsets;
   }

   public void setIcmpCode(int icmpCode) {
      _icmpCode = icmpCode;
   }

   public void setIcmpType(int icmpType) {
      _icmpType = icmpType;
   }

   public void setIpProtocols(Set<IpProtocol> ipProtocols) {
      _ipProtocols.clear();
      _ipProtocols.addAll(ipProtocols);
   }

   public void setNegate(boolean negate) {
      _negate = negate;
   }

   public void setNotDscps(SortedSet<Integer> notDscps) {
      _notDscps = notDscps;
   }

   public void setNotDstIps(SortedSet<IpWildcard> notDstIps) {
      _notDstIps = notDstIps;
   }

   public void setNotDstPorts(SortedSet<SubRange> notDstPorts) {
      _notDstPorts = notDstPorts;
   }

   public void setNotEcns(SortedSet<Integer> notEcns) {
      _notEcns = notEcns;
   }

   public void setNotFragmentOffsets(SortedSet<SubRange> notFragmentOffsets) {
      _notFragmentOffsets = notFragmentOffsets;
   }

   public void setNotIcmpCode(int notIcmpCode) {
      _notIcmpCode = notIcmpCode;
   }

   public void setNotIcmpType(int notIcmpType) {
      _notIcmpType = notIcmpType;
   }

   public void setNotIpProtocols(Set<IpProtocol> notIpProtocols) {
      _notIpProtocols.clear();
      _notIpProtocols.addAll(notIpProtocols);
   }

   public void setNotSrcIps(SortedSet<IpWildcard> notSrcIps) {
      _notSrcIps = notSrcIps;
   }

   public void setNotSrcPorts(SortedSet<SubRange> notSrcPorts) {
      _notSrcPorts = notSrcPorts;
   }

   public void setSrcIps(SortedSet<IpWildcard> srcIps) {
      _srcIps = srcIps;
   }

   public void setSrcOrDstIps(SortedSet<IpWildcard> srcOrDstIps) {
      _srcOrDstIps = srcOrDstIps;
   }

   public void setSrcOrDstPorts(SortedSet<SubRange> srcOrDstPorts) {
      _srcOrDstPorts = srcOrDstPorts;
   }

   public void setSrcPorts(SortedSet<SubRange> srcPorts) {
      _srcPorts = srcPorts;
   }

   public void setStates(Set<State> states) {
      _states = states;
   }

   public void setTcpFlags(List<TcpFlags> tcpFlags) {
      _tcpFlags = tcpFlags;
   }

   @Override
   public String toString() {
      return "[Protocols:" + _ipProtocols.toString() + ", SrcIps:" + _srcIps
            + ", NotSrcIps:" + _notSrcIps + ", DstIps:" + _dstIps
            + ", NotDstIps:" + _notDstIps + ", SrcOrDstIps:" + _srcOrDstIps
            + ", SrcPorts:" + _srcPorts + ", NotSrcPorts:" + _notSrcPorts
            + ", DstPorts:" + _dstPorts + ", NotDstPorts:" + _notDstPorts
            + ", SrcOrDstPorts:" + _srcOrDstPorts + ", Dscps: "
            + _dscps.toString() + ", NotDscps: " + _notDscps.toString()
            + ", Ecns: " + _ecns.toString() + ", NotEcns: "
            + _notEcns.toString() + ", FragmentOffsets: "
            + _fragmentOffsets.toString() + ", NotFragmentOffsets: "
            + _notFragmentOffsets.toString() + ", IcmpType:" + _icmpType
            + ", NotIcmpType:" + _notIcmpType + ", IcmpCode:" + _icmpCode
            + ", NotIcmpCode:" + _notIcmpCode + ", States:"
            + _states.toString() + ", TcpFlags:" + _tcpFlags.toString() + "]";
   }

   public final boolean unrestricted() {
      boolean ret = _dscps.isEmpty() && _notDscps.isEmpty()
            && _dstIps.isEmpty() && _notDstIps.isEmpty() && _dstPorts.isEmpty()
            && _notDstPorts.isEmpty() && _ecns.isEmpty() && _notEcns.isEmpty()
            && _fragmentOffsets.isEmpty() && _notFragmentOffsets.isEmpty()
            && _icmpCode == IcmpCode.UNSET && _notIcmpCode == IcmpCode.UNSET
            && _icmpType == IcmpType.UNSET && _notIcmpType == IcmpType.UNSET
            && _ipProtocols.isEmpty() && _notIpProtocols.isEmpty()
            && _srcIps.isEmpty() && _notSrcIps.isEmpty()
            && _srcOrDstIps.isEmpty() && _srcOrDstPorts.isEmpty()
            && _srcPorts.isEmpty() && _notSrcPorts.isEmpty()
            && _states.isEmpty() && _tcpFlags.isEmpty();
      return ret;
   }

}
