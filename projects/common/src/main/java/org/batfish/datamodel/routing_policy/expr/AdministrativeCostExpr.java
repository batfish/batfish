package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import org.batfish.datamodel.routing_policy.Environment;

/**
 * Expression that evaluates to an administrative cost value. Used by {@link
 * org.batfish.datamodel.routing_policy.statement.SetAdministrativeCost}.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class AdministrativeCostExpr implements Serializable {

  /**
   * Evaluate this expression in the given environment.
   *
   * @param environment The environment in which to evaluate
   * @return The resulting administrative cost value
   */
  public abstract long evaluate(Environment environment);
}
