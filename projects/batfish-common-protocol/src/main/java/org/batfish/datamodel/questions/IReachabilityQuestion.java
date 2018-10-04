package org.batfish.datamodel.questions;

import java.util.SortedSet;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Protocol;

public interface IReachabilityQuestion extends IQuestion {

  void setActions(SortedSet<FlowDisposition> actionSet);

  void setDstIps(SortedSet<IpWildcard> singleton);

  void setDstProtocols(SortedSet<Protocol> protocols);

  void setIngressInterfaces(InterfacesSpecifier ingressInterfaces);

  void setIngressNodes(NodesSpecifier ingressNodes);

  void setNotDstProtocols(SortedSet<Protocol> protocols);
}
