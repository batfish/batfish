package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.RoutesExprVisitor;

/** A {@link RoutesExpr} that evaluates to the routes in the main RIB. */
@ParametersAreNonnullByDefault
public final class MainRibRoutes implements RoutesExpr {

  @Nonnull
  public static MainRibRoutes instance() {
    return INSTANCE;
  }

  @Override
  public <T, U> T accept(RoutesExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMainRibRoutes(this, arg);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof MainRibRoutes;
  }

  @Override
  public int hashCode() {
    // randomly generated
    return 0xa08d2257;
  }

  private static final MainRibRoutes INSTANCE = new MainRibRoutes();

  @JsonCreator
  @Nonnull
  private static MainRibRoutes create() {
    return INSTANCE;
  }

  private MainRibRoutes() {}
}
