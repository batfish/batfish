package batfish.representation.juniper;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import batfish.representation.LineAction;
import batfish.util.SubRange;
import batfish.util.Util;

public class ExtendedAccessListLine implements Serializable {

   private static final long serialVersionUID = 1L;


   private LineAction _ala;
   private String _dstIp;
   private List<SubRange> _dstPortRanges;
   private String _dstWildcard;
   private String _id;
   private int _protocol;
   private String _srcIp;
   private List<SubRange> _srcPortRanges;
   private String _srcWildcard;

   public ExtendedAccessListLine(LineAction ala, int protocol, String srcIp,
         String srcWildcard, String dstIp, String dstWildcard,
         List<SubRange> srcPortRanges, List<SubRange> dstPortRanges) {
      _ala = ala;
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
      if (dstPortRanges == null) {
            dstPortRanges = Collections.singletonList(new SubRange(0, 65535));
      }
   }

   public String getDestinationIP() {
      return _dstIp;
   }

   public String getDestinationWildcard() {
      return _dstWildcard;
   }

   public List<SubRange> getDstPortRanges() {
      return _dstPortRanges;
   }

   public String getID() {
      return _id;
   }

   public LineAction getLineAction() {
      return _ala;
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

   public List<SubRange> getSrcPortRanges() {
      return _srcPortRanges;
   }

   public void setDestinationIP(String d) {
      _dstIp = d;
   }

   public void setDestinationWildcard(String d) {
      _dstWildcard = d;
   }

   public void setID(String i) {
      _id = i;
   }

   public void setLineAction(LineAction la) {
      _ala = la;
   }

   public void setPortRange(List<SubRange> sr) {
      _srcPortRanges = sr;
   }

   public void setProtocol(int p) {
      _protocol = p;
   }

   public void setSourceIP(String s) {
      _srcIp = s;
   }

   public void setSourceWildcard(String s) {
      _srcWildcard = s;
   }

   @Override
   public String toString() {
      String protocolName = Util.getProtocolName(_protocol);
      return "[Action:"
            + _ala
            + ", Protocol:"
            + (protocolName != null ? protocolName + "(" + _protocol + ")"
                  : _protocol) + ", SourceIp:" + _srcIp + ", SourceWildcard:"
            + _srcWildcard + ", DestinationIp:" + _dstIp
            + ", DestinationWildcard:" + _dstWildcard + ", PortRange:"
            + _srcPortRanges + "]";
   }

}
