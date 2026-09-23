package org.batfish.representation.juniper;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** A {@code then damping} action in a Junos routing policy. */
@ParametersAreNonnullByDefault
public final class PsThenDamping extends PsThen {

  public PsThenDamping(String profile) {
    _profile = profile;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    // TODO: Model BGP route-flap damping.
    // https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/damping-edit-policy-options.html
  }

  public @Nonnull String getProfile() {
    return _profile;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PsThenDamping)) {
      return false;
    }
    PsThenDamping that = (PsThenDamping) o;
    return _profile.equals(that._profile);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_profile);
  }

  private final @Nonnull String _profile;
}
