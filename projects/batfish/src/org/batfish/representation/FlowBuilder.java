package org.batfish.representation;

import org.batfish.common.BatfishException;

public class FlowBuilder {

   private Ip _dstIp;

   private Integer _dstPort;

   private String _ingressNode;

   private IpProtocol _ipProtocol;

   private Ip _srcIp;

   private Integer _srcPort;

   private String _tag;

   public FlowBuilder() {
      _dstIp = Ip.ZERO;
      _dstPort = 0;
      _ipProtocol = IpProtocol.fromNumber(0);
      _srcIp = Ip.ZERO;
      _srcPort = 0;
   }

   public Flow build() {
      if (_ingressNode == null) {
         throw new BatfishException(
               "Cannot build flow without at least specifying ingress node");
      }
      if (_tag == null) {
         throw new BatfishException("Cannot build flow without specifying tag");
      }
      return new Flow(_ingressNode, _srcIp, _dstIp, _srcPort, _dstPort,
            _ipProtocol, _tag);
   }

   public void setDstIp(Ip dstIp) {
      _dstIp = dstIp;
   }

   public void setDstPort(int dstPort) {
      _dstPort = dstPort;
   }

   public void setIngressNode(String ingressNode) {
      _ingressNode = ingressNode;
   }

   public void setIpProtocol(IpProtocol ipProtocol) {
      _ipProtocol = ipProtocol;
   }

   public void setSrcIp(Ip srcIp) {
      _srcIp = srcIp;
   }

   public void setSrcPort(int srcPort) {
      _srcPort = srcPort;
   }

   public void setTag(String tag) {
      _tag = tag;
   }

}
