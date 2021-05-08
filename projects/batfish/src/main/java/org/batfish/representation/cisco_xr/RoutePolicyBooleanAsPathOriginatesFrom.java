package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;

/**
 * A route-policy boolean expression that is true iff the route has an as-path whose last ASes are
 * given by provided AS ranges. * Duplicates are ignored unless this match is 'exact'.
 */
@ParametersAreNonnullByDefault
public final class RoutePolicyBooleanAsPathOriginatesFrom extends RoutePolicyBoolean {

  @SafeVarargs
  public RoutePolicyBooleanAsPathOriginatesFrom(boolean exact, Range<Long>... ranges) {
    this(exact, Arrays.asList(ranges));
  }

  public RoutePolicyBooleanAsPathOriginatesFrom(boolean exact, Iterable<Range<Long>> ranges) {
    _ranges = ImmutableList.copyOf(ranges);
    _exact = exact;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RoutePolicyBooleanAsPathOriginatesFrom)) {
      return false;
    }
    RoutePolicyBooleanAsPathOriginatesFrom that = (RoutePolicyBooleanAsPathOriginatesFrom) o;
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
