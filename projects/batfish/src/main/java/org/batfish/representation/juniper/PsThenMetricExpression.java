package org.batfish.representation.juniper;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** A metric expression action in a Junos routing policy. */
@ParametersAreNonnullByDefault
public final class PsThenMetricExpression extends PsThen {

  public enum Source {
    METRIC,
    METRIC2
  }

  public enum Target {
    METRIC,
    METRIC2
  }

  public PsThenMetricExpression(Target target, Source source, long multiplier, long offset) {
    _target = target;
    _source = source;
    _multiplier = multiplier;
    _offset = offset;
  }

  public long getMultiplier() {
    return _multiplier;
  }

  public long getOffset() {
    return _offset;
  }

  public @Nonnull Source getSource() {
    return _source;
  }

  public @Nonnull Target getTarget() {
    return _target;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    // TODO: Model arithmetic expressions over Junos metric and metric2.
    // https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/metric-edit-policy-options-policy-statement-then.html
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PsThenMetricExpression)) {
      return false;
    }
    PsThenMetricExpression that = (PsThenMetricExpression) o;
    return _target == that._target
        && _source == that._source
        && _multiplier == that._multiplier
        && _offset == that._offset;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_target, _source, _multiplier, _offset);
  }

  private final long _multiplier;
  private final long _offset;
  private final @Nonnull Source _source;
  private final @Nonnull Target _target;
}
