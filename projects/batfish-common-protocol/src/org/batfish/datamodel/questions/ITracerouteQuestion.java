package org.batfish.datamodel.questions;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;

public interface ITracerouteQuestion extends IQuestion {

   static final String NAME = "traceroute";

   void setDstIp(Ip dstIp);

   void setDstPort(Integer dstPort);

   void setIngressNode(String ingressNode);

   void setIngressVrf(String ingressVrf);

   void setIpProtocol(IpProtocol ipProtocol);

}
