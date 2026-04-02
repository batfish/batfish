package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class FibEntry implements Serializable {

  private final @Nonnull FibAction _action;
  //  private final @Nonnull List<AbstractRoute> _resolutionSteps;
  private final @Nonnull AbstractRoute _topLevelRoute;
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
    // TODO: currently only keeping the top level route. Until there is another use case for
    //       resolution steps, the rest are not used.
    // _resolutionSteps = ImmutableList.copyOf(resolutionSteps);
    _topLevelRoute = resolutionSteps.get(0);
  }

  /** The action to take when this entry is matched. */
  public @Nonnull FibAction getAction() {
    return _action;
  }

  /** Return the top level route for this entry (before recursive resolution) */
  public @Nonnull AbstractRoute getTopLevelRoute() {
    return _topLevelRoute;
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
        && _topLevelRoute.equals(rhs._topLevelRoute);
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = _action.hashCode();
      h = 31 * h + _topLevelRoute.hashCode();
      _hashCode = h;
    }
    return h;
  }
}
