package org.batfish.representation.juniper;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** An {@code aigp-adjust} action in a Junos routing policy. */
@ParametersAreNonnullByDefault
public final class PsThenAigpAdjust extends PsThen {

  public enum Operator {
    ADD,
    DIVIDE,
    MULTIPLY,
    SUBTRACT
  }

  public PsThenAigpAdjust(Operator operator, @Nullable BigInteger value) {
    _operator = operator;
    _value = value;
  }

  public @Nonnull Operator getOperator() {
    return _operator;
  }

  /**
   * Returns the literal operand, or {@code null} when the operand is {@code
   * distance-to-protocol-nexthop}.
   */
  public @Nullable BigInteger getValue() {
    return _value;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    // TODO: Model AIGP metrics in routing policies.
    // https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/aigp-adjust-edit-policy-options.html
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PsThenAigpAdjust)) {
      return false;
    }
    PsThenAigpAdjust that = (PsThenAigpAdjust) o;
    return _operator == that._operator && Objects.equals(_value, that._value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_operator, _value);
  }

  private final @Nonnull Operator _operator;
  private final @Nullable BigInteger _value;
}
