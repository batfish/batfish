package org.batfish.datamodel.questions;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Protocol;

public interface ITracerouteQuestion extends IQuestion {

   static final String NAME = "traceroute";

   void setDstIp(Ip dstIp);

   void setDstProtocol(Protocol protocol);

   void setIngressNode(String ingressNode);

   void setIngressVrf(String ingressVrf);

}
