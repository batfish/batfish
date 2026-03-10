package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.HasReadableAsPath;
import org.batfish.datamodel.routing_policy.Environment;

/** Represents the most "recent" AS of the route. */
public final class LastAs extends AsExpr {

  private static final LastAs INSTANCE = new LastAs();

  public static @Nonnull LastAs instance() {
    return INSTANCE;
  }

  private LastAs() {}

  @Override
  public boolean equals(Object obj) {
    return obj instanceof LastAs;
  }

  @Override
  public long evaluate(Environment environment) {
    AsPath asPath;
    if (environment.getUseOutputAttributes()
        && environment.getOutputRoute() instanceof BgpRoute.Builder<?, ?>) {
      BgpRoute.Builder<?, ?> bgpRouteBuilder =
          (BgpRoute.Builder<?, ?>) environment.getOutputRoute();
      asPath = bgpRouteBuilder.getAsPath();
    } else if (environment.getReadFromIntermediateBgpAttributes()) {
      asPath = environment.getIntermediateBgpAttributes().getAsPath();
    } else {
      // caller should guarantee this holds if this branch is reached
      assert environment.getOriginalRoute() instanceof HasReadableAsPath;
      HasReadableAsPath bgpRoute = (HasReadableAsPath) environment.getOriginalRoute();
      asPath = bgpRoute.getAsPath();
    }
    List<AsSet> asSets = asPath.getAsSets();
    // Caller must guarantee this is not evaluated when as-path is empty.
    checkState(!asSets.isEmpty(), "Cannot be evaluated with an empty as-path.");
    SortedSet<Long> lastAsSetAsns = asSets.get(0).getAsns();
    // TODO: See if clients of AsExpr should really be provided the entire AsSet instead of a
    //       single AS. For now, arbitrarily use lowest AS in set.
    return lastAsSetAsns.first();
  }

  @Override
  public int hashCode() {
    return 0x207D97F5; // randomly generated
  }
}
