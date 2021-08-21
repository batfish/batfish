package org.batfish.vendor.check_point_management;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.List;
import javax.annotation.Nonnull;

/** Indicates that {@link NatRule} should be applied to listed targets. */
public class ListNatInstallTarget implements NatInstallTarget {

  ListNatInstallTarget(List<Uid> targets) {
    _targets = targets;
  }

  public @Nonnull List<Uid> getTargets() {
    return _targets;
  }

  private final @Nonnull List<Uid> _targets;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof ListNatInstallTarget)) {
      return false;
    }
    ListNatInstallTarget that = (ListNatInstallTarget) o;
    return _targets.equals(that._targets);
  }

  @Override
  public int hashCode() {
    return _targets.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_targets", _targets).toString();
  }
}
