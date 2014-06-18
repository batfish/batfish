package batfish.representation.cisco;

import java.util.Collections;
import java.util.List;

import batfish.representation.LineAction;
import batfish.util.SubRange;
import batfish.util.Util;

public class ExtendedAccessListLine {

   private String _srcIp;
   private String _srcWildcard;
   private LineAction _action;
   private int _protocol;
   private String _dstIp;
   private String _dstWildcard;
   private List<SubRange> _srcPortRanges;
   private List<SubRange> _dstPortRanges;

   public ExtendedAccessListLine(LineAction action, int protocol,
         String srcIp, String srcWildcard, String dstIp, String dstWildcard,
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

   public List<SubRange> getSrcPortRanges() {
      return _srcPortRanges;
   }

   public LineAction getAction() {
      return _action;
   }

   public int getProtocol() {
      return _protocol;
   }

   public String getSourceIP() {
      return _srcIp;
   }

   public String getSourceWildcard() {
      return _srcWildcard;
   }

   public String getDestinationIP() {
      return _dstIp;
   }

   public String getDestinationWildcard() {
      return _dstWildcard;
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

   public List<SubRange> getDstPortRange() {
      return _dstPortRanges;
   }

}
