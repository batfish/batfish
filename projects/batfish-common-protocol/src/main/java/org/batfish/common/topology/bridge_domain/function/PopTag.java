package org.batfish.common.topology.bridge_domain.function;

import org.batfish.common.topology.bridge_domain.edge.L2ToBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.PhysicalToL2;

/** Helper interface to allow simplification of {@link PopTagImpl}. */
public interface PopTag extends PhysicalToL2.Function, L2ToBridgeDomain.Function {}
