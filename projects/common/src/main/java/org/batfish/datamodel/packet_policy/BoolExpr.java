package org.batfish.datamodel.packet_policy;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

/** Represents a {@link PacketPolicy} boolean expression */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface BoolExpr extends Serializable {

  <T> T accept(BoolExprVisitor<T> tBoolExprVisitor);
}
