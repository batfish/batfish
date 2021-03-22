package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.RibExprVisitor;

/** A {@link RibExpr} that evaluates to the main RIB. */
@ParametersAreNonnullByDefault
public final class MainRib implements RibExpr {

  @Nonnull
  public static MainRib instance() {
    return INSTANCE;
  }

  @Override
  public <T, U> T accept(RibExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMainRib(this, arg);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof MainRib;
  }

  @Override
  public int hashCode() {
    // randomly generated
    return 0xa08d2257;
  }

  private static final MainRib INSTANCE = new MainRib();

  @JsonCreator
  @Nonnull
  private static MainRib create() {
    return INSTANCE;
  }

  private MainRib() {}
}
