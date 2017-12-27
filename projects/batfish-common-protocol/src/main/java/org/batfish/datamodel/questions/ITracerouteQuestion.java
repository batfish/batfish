package org.batfish.datamodel.questions;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Protocol;

public interface ITracerouteQuestion extends IQuestion {

  void setDst(String dst);

  void setDstProtocol(Protocol protocol);

  void setIngressNode(String ingressNode);

  void setIngressVrf(String ingressVrf);
}
