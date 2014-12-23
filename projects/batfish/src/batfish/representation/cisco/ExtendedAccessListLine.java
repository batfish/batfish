package batfish.representation.cisco;

import java.io.Serializable;
import java.util.List;

import batfish.representation.Ip;
import batfish.representation.IpProtocol;
import batfish.representation.LineAction;
import batfish.util.SubRange;

public class ExtendedAccessListLine implements Serializable {

   private static final long serialVersionUID = 1L;

   private LineAction _action;
   private Ip _dstIp;
   private List<SubRange> _dstPortRanges;
   private Ip _dstWildcard;
   private IpProtocol _protocol;
   private Ip _srcIp;
   private List<SubRange> _srcPortRanges;
   private Ip _srcWildcard;

   public ExtendedAccessListLine(LineAction action, IpProtocol protocol,
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

   public IpProtocol getProtocol() {
      return _protocol;
   }

   public Ip getSourceIp() {
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
      String protocolName = _protocol.name();
      return "[Action:"
            + _action
            + ", Protocol:"
            + (protocolName != null ? protocolName + "(" + _protocol + ")"
                  : _protocol) + ", SourceIp:" + _srcIp + ", SourceWildcard:"
            + _srcWildcard + ", DestinationIp:" + _dstIp
            + ", DestinationWildcard:" + _dstWildcard + ", PortRange:"
            + _srcPortRanges + "]";
   }

}
