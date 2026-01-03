package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import org.batfish.datamodel.eigrp.EigrpMetricValues;
import org.batfish.datamodel.routing_policy.Environment;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class EigrpMetricExpr implements Serializable {

  @Override
  public abstract boolean equals(Object obj);

  public abstract EigrpMetricValues evaluate(Environment env);

  @Override
  public abstract int hashCode();
}
