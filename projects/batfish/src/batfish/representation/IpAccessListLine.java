package batfish.representation;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import batfish.util.SubRange;
import batfish.util.Util;

public class IpAccessListLine implements Serializable {

   private static final long serialVersionUID = 1L;

   private LineAction _action;
   private Ip _dstIp;
   private List<SubRange> _dstPortRanges;
   private Ip _dstWildcard;
   private int _protocol;
   private Ip _srcIp;
   private List<SubRange> _srcPortRanges;
   private Ip _srcWildcard;

   public IpAccessListLine(LineAction ala, int protocol, Ip srcIp,
         Ip srcWildcard, Ip dstIp, Ip dstWildcard,
         List<SubRange> srcPortRanges, List<SubRange> dstPortRanges) {
      _action = ala;
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
         _srcPortRanges = Collections.singletonList(new SubRange(0, 65535));
      }
      if (dstPortRanges == null
            && (Util.getProtocolName(protocol).equals("tcp") || Util
                  .getProtocolName(protocol).equals("udp"))) {
         _dstPortRanges = Collections.singletonList(new SubRange(0, 65535));
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

   public List<SubRange> getDstPortRanges() {
      return _dstPortRanges;
   }

   public String getIFString(int indentLevel) {

      String dstPortStr = "-";

      if (_dstPortRanges != null) {
         dstPortStr = "";
         for (SubRange sr : _dstPortRanges) {
            dstPortStr += " " + sr;
         }
      }

      String srcPortStr = "-";

      if (_srcPortRanges != null) {
         srcPortStr = "";
         for (SubRange sr : _srcPortRanges) {
            srcPortStr += " " + sr;
         }
      }

      String retString = String
            .format(
                  "%sIpAccessListLine SrcIp %s SrcWildCard %s SrcPortStr %s DstIp %s DstWildCard %s DstPortStr %s Proto %s Action %s",
                  Util.getIndentString(indentLevel), _srcIp, _srcWildcard,
                  srcPortStr, _dstIp, _dstWildcard, dstPortStr, _protocol,
                  _action);

      return retString;

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
            + ", DestinationWildcard:" + _dstWildcard + ", SrcPortRange:"
            + _srcPortRanges + ", DstPortRange:" + _dstPortRanges + "]";
   }

}
