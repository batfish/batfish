package org.batfish.minesweeper.communities;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.MainRibRoutes;
import org.batfish.datamodel.visitors.RoutesExprVisitor;
import org.batfish.minesweeper.CommunityVar;

public final class RoutesExprVarCollector
    implements RoutesExprVisitor<Set<CommunityVar>, Configuration> {

  @Nonnull
  public static RoutesExprVarCollector instance() {
    return INSTANCE;
  }

  @Override
  public Set<CommunityVar> visitMainRibRoutes(MainRibRoutes mainRibRoutes, Configuration arg) {
    return ImmutableSet.of();
  }

  private static final RoutesExprVarCollector INSTANCE = new RoutesExprVarCollector();

  private RoutesExprVarCollector() {}
}
