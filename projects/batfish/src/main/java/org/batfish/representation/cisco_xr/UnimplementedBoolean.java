package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;

/** Stand-in for unimplemented route-policy boolean constructs */
@ParametersAreNonnullByDefault
public final class UnimplementedBoolean extends RoutePolicyBoolean {

  public static @Nonnull UnimplementedBoolean instance() {
    return INSTANCE;
  }

  @Override
  public BooleanExpr toBooleanExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return BooleanExprs.FALSE;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof UnimplementedBoolean;
  }

  @Override
  public int hashCode() {
    // randomly generated
    return 0x1D6E93B2;
  }

  private static @Nonnull UnimplementedBoolean INSTANCE = new UnimplementedBoolean();
}
