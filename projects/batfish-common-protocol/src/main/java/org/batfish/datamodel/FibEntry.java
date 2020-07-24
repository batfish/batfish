package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Iterables;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class FibEntry implements Serializable {

  @Nonnull private final FibAction _action;
  @Nonnull private final List<AbstractRoute> _resolutionSteps;
  private transient int _hashCode;

  /**
   * Create a new FIB entry with the given nextHop/ARP IP, interface name, and resolution steps.
   *
   * <p>Note that at least one resolution step is required (even if it is the route itself, e.g. a
   * connected route)
   */
  public FibEntry(FibAction action, List<AbstractRoute> resolutionSteps) {
    checkArgument(
        !resolutionSteps.isEmpty(), "FIB resolution steps must contain at least one route");
    _action = action;
    _resolutionSteps = resolutionSteps;
  }

  /** The action to take when this entry is matched. */
  public @Nonnull FibAction getAction() {
    return _action;
  }

  /** A chain of routes that explains how the top route was resolved */
  @Nonnull
  public List<AbstractRoute> getResolutionSteps() {
    return _resolutionSteps;
  }

  /** Return the top level route for this entry (before recursive resolution) */
  @Nonnull
  public AbstractRoute getTopLevelRoute() {
    return _resolutionSteps.get(0);
  }

  /** Return the final resolved route */
  public AbstractRoute getResolvedToRoute() {
    return Iterables.getLast(_resolutionSteps);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FibEntry)) {
      return false;
    }
    FibEntry rhs = (FibEntry) o;
    return (_hashCode == rhs._hashCode || _hashCode == 0 || rhs._hashCode == 0)
        && _action.equals(rhs._action)
        && _resolutionSteps.equals(rhs._resolutionSteps);
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = _action.hashCode();
      h = 31 * h + _resolutionSteps.hashCode();
      _hashCode = h;
    }
    return h;
  }
}
