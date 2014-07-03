package batfish.representation.cisco;

import java.util.Collections;
import java.util.List;

import batfish.representation.Ip;
import batfish.representation.LineAction;
import batfish.util.SubRange;
import batfish.util.Util;

public class ExtendedAccessListLine {

   private LineAction _action;
   private Ip _dstIp;
   private List<SubRange> _dstPortRanges;
   private Ip _dstWildcard;
   private int _protocol;
   private Ip _srcIp;
   private List<SubRange> _srcPortRanges;
   private Ip _srcWildcard;

   public ExtendedAccessListLine(LineAction action, int protocol,
         Ip srcIp, Ip srcWildcard, Ip dstIp, Ip dstWildcard,
         List<SubRange> srcPortRanges, List<SubRange> dstPortRanges) {
      _action = action;
      _protocol = protocol;
      _srcIp = srcIp;
      _srcWildcard = srcWildcard;
      _dstIp = dstIp;
      _dstWildcard = dstWildcard;
      _srcPortRanges = srcPortRanges;
      _dstPortRanges = dstPortRanges;
      if (srcPortRanges == null
            && (Util.getProtocolName(protocol).equals("tcp") || Util
                  .getProtocolName(protocol).equals("udp"))) {
         srcPortRanges = Collections.singletonList(new SubRange(0, 65535));
      }
      if (dstPortRanges == null
            && (Util.getProtocolName(protocol).equals("tcp") || Util
                  .getProtocolName(protocol).equals("udp"))) {
         dstPortRanges = Collections.singletonList(new SubRange(0, 65535));
      }
   }

   public LineAction getAction() {
      return _action;
   }

   public Ip getDestinationIP() {
      return _dstIp;
   }

   public Ip getDestinationWildcard() {
      return _dstWildcard;
   }

   public List<SubRange> getDstPortRange() {
      return _dstPortRanges;
   }

   public int getProtocol() {
      return _protocol;
   }

   public Ip getSourceIP() {
      return _srcIp;
   }

   public Ip getSourceWildcard() {
      return _srcWildcard;
   }

   public List<SubRange> getSrcPortRanges() {
      return _srcPortRanges;
   }

   @Override
   public String toString() {
      String protocolName = Util.getProtocolName(_protocol);
      return "[Action:"
            + _action
            + ", Protocol:"
            + (protocolName != null ? protocolName + "(" + _protocol + ")"
                  : _protocol) + ", SourceIp:" + _srcIp + ", SourceWildcard:"
            + _srcWildcard + ", DestinationIp:" + _dstIp
            + ", DestinationWildcard:" + _dstWildcard + ", PortRange:" + _srcPortRanges + "]";
   }

}
