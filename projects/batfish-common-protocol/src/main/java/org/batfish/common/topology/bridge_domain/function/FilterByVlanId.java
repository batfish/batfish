package org.batfish.common.topology.bridge_domain.function;

import org.batfish.common.topology.bridge_domain.edge.BridgeDomainToBridgedL3;
import org.batfish.common.topology.bridge_domain.edge.BridgeDomainToL2;
import org.batfish.common.topology.bridge_domain.edge.BridgeDomainToL2Vni;

/** Helper interface to allow simplification of {@link FilterByVlanIdImpl}. */
public interface FilterByVlanId
    extends BridgeDomainToL2.Function,
        BridgeDomainToBridgedL3.Function,
        BridgeDomainToL2Vni.Function {}
