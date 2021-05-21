package org.batfish.representation.cisco_xr;

import static org.batfish.datamodel.routing_policy.expr.IntComparator.EQ;

import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.as_path.HasAsPathLength;
import org.batfish.datamodel.routing_policy.as_path.InputAsPath;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;

/**
 * Checks whether the AS Path is local to this AS. Per the below documentation, this is when an AS
 * Path is empty.
 *
 * <p>See
 * https://www.cisco.com/c/en/us/td/docs/routers/asr9000/software/asr9k_r4-0/routing/command/reference/rr40asr9kbook_chapter8.html#wp1504108348
 */
public class RoutePolicyBooleanAsPathIsLocal extends RoutePolicyBoolean {

  public static @Nonnull RoutePolicyBooleanAsPathIsLocal instance() {
    return INSTANCE;
  }

  @Override
  public BooleanExpr toBooleanExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return MatchAsPath.of(
        InputAsPath.instance(), HasAsPathLength.of(new IntComparison(EQ, new LiteralInt(0))));
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof RoutePolicyBooleanAsPathIsLocal;
  }

  @Override
  public int hashCode() {
    // randomly generated
    return 0xDA3A91D1;
  }

  private static final @Nonnull RoutePolicyBooleanAsPathIsLocal INSTANCE =
      new RoutePolicyBooleanAsPathIsLocal();

  private RoutePolicyBooleanAsPathIsLocal() {}
}
