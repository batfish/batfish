package org.batfish.representation.juniper;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a firewall filter term "then policer" action. */
@ParametersAreNonnullByDefault
public final class FwThenPolicer implements FwThen {

  private final String _policerName;

  public FwThenPolicer(String policerName) {
    _policerName = policerName;
  }

  public @Nonnull String getPolicerName() {
    return _policerName;
  }

  @Override
  public <T> T accept(FwThenVisitor<T> visitor) {
    return visitor.visitFwThenPolicer(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FwThenPolicer)) {
      return false;
    }
    FwThenPolicer that = (FwThenPolicer) o;
    return Objects.equals(_policerName, that._policerName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_policerName);
  }
}
