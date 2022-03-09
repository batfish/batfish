package org.batfish.common.topology.bridge_domain.function;

import org.batfish.common.topology.bridge_domain.edge.BridgeDomainToL2;
import org.batfish.common.topology.bridge_domain.edge.L2ToBridgeDomain;

/** Helper interface to allow simplification of {@link TranslateVlanImpl}. */
public interface TranslateVlan extends BridgeDomainToL2.Function, L2ToBridgeDomain.Function {}
