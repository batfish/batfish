package org.batfish.datamodel.questions;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;

public interface ITracerouteQuestion extends IQuestion {

   static final String NAME = "traceroute";

   void setDstIp(Ip ip);

   void setDstPort(Integer port);

   void setIngressNode(String srcNode);

   void setIpProtocol(IpProtocol ipProtocol);

}
