package org.batfish.datamodel.packet_policy;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

/**
 * Policy expression that represents a VRF, e.g, for a {@link FibLookup}. When evaluated, should
 * resolve to a concrete VRF identifier.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface VrfExpr extends Serializable {

  <T> T accept(VrfExprVisitor<T> visitor);
}
