package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.RoutesExprVisitor;

/** An expression representing a collection of routes. */
@ParametersAreNonnullByDefault
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface RoutesExpr extends Serializable {

  <T, U> T accept(RoutesExprVisitor<T, U> visitor, U arg);
}
