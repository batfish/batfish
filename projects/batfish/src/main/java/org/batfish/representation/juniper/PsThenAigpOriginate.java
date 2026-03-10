package org.batfish.representation.juniper;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** Represents an unsupported "aigp-originate" action in a {@link PsTerm} */
@ParametersAreNonnullByDefault
public final class PsThenAigpOriginate extends PsThen {

  private final @Nullable Long _distance;

  public PsThenAigpOriginate(@Nullable Long distance) {
    _distance = distance;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    // AIGP originate is not supported - no-op
  }

  public @Nullable Long getDistance() {
    return _distance;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PsThenAigpOriginate)) {
      return false;
    }
    PsThenAigpOriginate that = (PsThenAigpOriginate) o;
    return Objects.equals(_distance, that._distance);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_distance);
  }
}
