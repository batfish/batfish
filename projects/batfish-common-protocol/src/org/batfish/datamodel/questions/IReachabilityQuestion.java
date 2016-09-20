package org.batfish.datamodel.questions;

import java.util.Set;

import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.SubRange;

public interface IReachabilityQuestion extends IQuestion {

   static final String NAME = "reachability";

   void setActions(Set<ForwardingAction> actionSet);

   void setDstIps(Set<IpWildcard> singleton);

   void setDstPorts(Set<SubRange> portRanges);

   void setIngressNodeRegex(String ingressNodeRegex);

   void setIpProtocols(Set<IpProtocol> singleton);

   void setNotDstPorts(Set<SubRange> portRanges);

   void setNotIpProtocols(Set<IpProtocol> singleton);

}
