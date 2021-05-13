package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;

/**
 * A route-policy boolean expression that is true iff the route has an as-path that passes through
 * ASes given by provided AS * ranges. Duplicates are ignored unless this match is 'exact'.
 */
@ParametersAreNonnullByDefault
public final class RoutePolicyBooleanAsPathPassesThrough extends RoutePolicyBoolean {

  @SafeVarargs
  @SuppressWarnings("varargs")
  public RoutePolicyBooleanAsPathPassesThrough(boolean exact, Range<Long>... ranges) {
    this(exact, ImmutableList.copyOf(ranges));
  }

  public RoutePolicyBooleanAsPathPassesThrough(boolean exact, Iterable<Range<Long>> ranges) {
    _ranges = ImmutableList.copyOf(ranges);
    _exact = exact;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RoutePolicyBooleanAsPathPassesThrough)) {
      return false;
    }
    RoutePolicyBooleanAsPathPassesThrough that = (RoutePolicyBooleanAsPathPassesThrough) o;
    return _exact == that._exact && _ranges.equals(that._ranges);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_exact, _ranges);
  }

  @Override
  public BooleanExpr toBooleanExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    // TODO: implement
    return BooleanExprs.FALSE;
  }

  private final boolean _exact;
  private final @Nonnull List<Range<Long>> _ranges;
}
