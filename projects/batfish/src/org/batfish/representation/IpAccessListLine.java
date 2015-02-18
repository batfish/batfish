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

   private String _invalidMessage;

   private Set<IpProtocol> _protocols;

   private Set<Prefix> _srcIpRanges;

   private List<SubRange> _srcPortRanges;

   public IpAccessListLine() {
      _protocols = EnumSet.noneOf(IpProtocol.class);
      _dstIpRanges = new TreeSet<Prefix>();
      _dstPortRanges = new ArrayList<SubRange>();
      _srcIpRanges = new TreeSet<Prefix>();
      _srcPortRanges = new ArrayList<SubRange>();
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

   public void setAction(LineAction action) {
      _action = action;
   }

   public void setInvalidMessage(String invalidMessage) {
      _invalidMessage = invalidMessage;
   }

   @Override
   public String toString() {
      return "[Action:" + _action + ", Protocols:" + _protocols.toString()
            + ", SourceIpRanges:" + _srcIpRanges + ", DestinationIpRanges:"
            + _dstIpRanges + ", SrcPortRanges:" + _srcPortRanges
            + ", DstPortRanges:" + _dstPortRanges + "]";
   }

}
