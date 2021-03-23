package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.RibExprVisitor;

/** An expression representing a collection of routes. */
@ParametersAreNonnullByDefault
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface RibExpr extends Serializable {

  <T, U> T accept(RibExprVisitor<T, U> visitor, U arg);
}
