package org.batfish.common.topology.bridge_domain.function;

import org.batfish.common.topology.bridge_domain.edge.BridgeDomainToBridgedL3;
import org.batfish.common.topology.bridge_domain.edge.PhysicalToL2;
import org.batfish.common.topology.bridge_domain.edge.PhysicalToNonBridgedL3;

/** Helper interface to allow simplification of {@link FilterByOuterTagImpl}. */
interface FilterByOuterTag
    extends PhysicalToL2.Function,
        PhysicalToNonBridgedL3.Function,
        BridgeDomainToBridgedL3.Function {}
