package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.routing_policy.Environment;

public class LiteralEigrpMetric extends EigrpMetricExpr {

  private static final long serialVersionUID = 1L;

  private EigrpMetric _metric;

  @JsonCreator
  private LiteralEigrpMetric() {}

  public LiteralEigrpMetric(@Nonnull EigrpMetric metric) {
    _metric = metric;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof LiteralEigrpMetric)) {
      return false;
    }

    LiteralEigrpMetric rhs = (LiteralEigrpMetric) obj;
    return Objects.equals(_metric, rhs._metric);
  }

  @Override
  public EigrpMetric evaluate(Environment env) {
    return _metric;
  }

  public EigrpMetric getMetric() {
    return _metric;
  }

  public void setMetric(EigrpMetric metric) {
    _metric = metric;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_metric);
  }
}
