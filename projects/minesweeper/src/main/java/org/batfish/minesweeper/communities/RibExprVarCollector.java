package org.batfish.minesweeper.communities;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.MainRib;
import org.batfish.datamodel.visitors.RibExprVisitor;
import org.batfish.minesweeper.CommunityVar;

public final class RibExprVarCollector implements RibExprVisitor<Set<CommunityVar>, Configuration> {

  @Nonnull
  public static RibExprVarCollector instance() {
    return INSTANCE;
  }

  @Override
  public Set<CommunityVar> visitMainRib(MainRib mainRib, Configuration arg) {
    return ImmutableSet.of();
  }

  private static final RibExprVarCollector INSTANCE = new RibExprVarCollector();

  private RibExprVarCollector() {}
}
