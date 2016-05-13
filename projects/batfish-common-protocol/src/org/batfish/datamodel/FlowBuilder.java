package org.batfish.datamodel;

import org.batfish.common.BatfishException;

public class FlowBuilder {

   private Integer _dscp;

   private Ip _dstIp;

   private Integer _dstPort;

   private Integer _ecn;

   private Integer _icmpCode;

   private Integer _icmpType;

   private String _ingressNode;

   private IpProtocol _ipProtocol;

   private Ip _srcIp;

   private Integer _srcPort;

   private String _tag;

   private Integer _tcpFlagsAck;

   private Integer _tcpFlagsCwr;

   private Integer _tcpFlagsEce;

   private Integer _tcpFlagsFin;

   private Integer _tcpFlagsPsh;

   private Integer _tcpFlagsRst;

   private Integer _tcpFlagsSyn;

   private Integer _tcpFlagsUrg;

   public FlowBuilder() {
      _dscp = 0;
      _dstIp = Ip.ZERO;
      _dstPort = 0;
      _ecn = 0;
      _ipProtocol = IpProtocol.IP;
      _srcIp = Ip.ZERO;
      _srcPort = 0;
      _icmpType = IcmpType.UNSET;
      _icmpCode = IcmpCode.UNSET;
      _tcpFlagsCwr = 0;
      _tcpFlagsEce = 0;
      _tcpFlagsUrg = 0;
      _tcpFlagsAck = 0;
      _tcpFlagsPsh = 0;
      _tcpFlagsRst = 0;
      _tcpFlagsSyn = 0;
      _tcpFlagsFin = 0;
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
            _ipProtocol, _dscp, _ecn, _icmpType, _icmpCode, _tcpFlagsCwr,
            _tcpFlagsEce, _tcpFlagsUrg, _tcpFlagsAck, _tcpFlagsPsh,
            _tcpFlagsRst, _tcpFlagsSyn, _tcpFlagsFin, _tag);
   }

   public void setDscp(Integer dscp) {
      _dscp = dscp;
   }

   public void setDstIp(Ip dstIp) {
      _dstIp = dstIp;
   }

   public void setDstPort(int dstPort) {
      _dstPort = dstPort;
   }

   public void setEcn(Integer ecn) {
      _ecn = ecn;
   }

   public void setIcmpCode(Integer icmpCode) {
      _icmpCode = icmpCode;
   }

   public void setIcmpType(Integer icmpType) {
      _icmpType = icmpType;
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

   public void setTcpFlagsAck(Integer tcpFlagsAck) {
      _tcpFlagsAck = tcpFlagsAck;
   }

   public void setTcpFlagsCwr(Integer tcpFlagsCwr) {
      _tcpFlagsCwr = tcpFlagsCwr;
   }

   public void setTcpFlagsEce(Integer tcpFlagsEce) {
      _tcpFlagsEce = tcpFlagsEce;
   }

   public void setTcpFlagsFin(Integer tcpFlagsFin) {
      _tcpFlagsFin = tcpFlagsFin;
   }

   public void setTcpFlagsPsh(Integer tcpFlagsPsh) {
      _tcpFlagsPsh = tcpFlagsPsh;
   }

   public void setTcpFlagsRst(Integer tcpFlagsRst) {
      _tcpFlagsRst = tcpFlagsRst;
   }

   public void setTcpFlagsSyn(Integer tcpFlagsSyn) {
      _tcpFlagsSyn = tcpFlagsSyn;
   }

   public void setTcpFlagsUrg(Integer tcpFlagsUrg) {
      _tcpFlagsUrg = tcpFlagsUrg;
   }

}
