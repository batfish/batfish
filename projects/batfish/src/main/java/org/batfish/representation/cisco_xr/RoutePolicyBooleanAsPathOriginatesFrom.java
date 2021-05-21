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
import org.batfish.datamodel.routing_policy.as_path.AsSetsMatchingRanges;
import org.batfish.datamodel.routing_policy.as_path.DedupedAsPath;
import org.batfish.datamodel.routing_policy.as_path.InputAsPath;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;

/**
 * A route-policy boolean expression that is true iff the route has an as-path whose last ASes are
 * given by provided AS ranges. * Duplicates are ignored unless this match is 'exact'.
 */
@ParametersAreNonnullByDefault
public final class RoutePolicyBooleanAsPathOriginatesFrom extends RoutePolicyBoolean {

  @SafeVarargs
  @SuppressWarnings("varargs")
  public RoutePolicyBooleanAsPathOriginatesFrom(boolean exact, Range<Long>... ranges) {
    this(exact, ImmutableList.copyOf(ranges));
  }

  public RoutePolicyBooleanAsPathOriginatesFrom(boolean exact, Iterable<Range<Long>> ranges) {
    _ranges = ImmutableList.copyOf(ranges);
    _exact = exact;
  }

  @Override
  public boolean equals(@Nullable Object o) {
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
    return MatchAsPath.of(
        _exact ? InputAsPath.instance() : DedupedAsPath.of(InputAsPath.instance()),
        AsSetsMatchingRanges.of(true, false, _ranges));
  }

  private final boolean _exact;
  private final @Nonnull List<Range<Long>> _ranges;
}
