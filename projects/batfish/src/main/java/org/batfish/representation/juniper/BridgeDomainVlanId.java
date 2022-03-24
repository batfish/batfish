package org.batfish.representation.juniper;

import java.io.Serializable;

/** Argument to {@code bridge-domain vlan-id}. */
public interface BridgeDomainVlanId extends Serializable {

  void accept(BridgeDomainVlanIdVoidVisitor visitor);
}
