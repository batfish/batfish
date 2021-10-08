package org.batfish.vendor.a10.representation;

import java.io.Serializable;

/** Represents a target for a load balancer virtual-server, e.g. IP address or interface address */
public interface VirtualServerTarget extends Serializable {

  <T> T accept(VirtualServerTargetVisitor<T> visitor);
}
