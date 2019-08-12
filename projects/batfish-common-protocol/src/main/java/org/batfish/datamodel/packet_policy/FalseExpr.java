package org.batfish.datamodel.packet_policy;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nullable;

/** The boolean false identity */
public final class FalseExpr implements BoolExpr {
  private static final FalseExpr INSTANCE = new FalseExpr();

  private FalseExpr() {}

  @JsonCreator
  public static FalseExpr instance() {
    return INSTANCE;
  }

  @Override
  public int hashCode() {
    return 0xb91c5aba;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return obj instanceof FalseExpr;
  }

  @Override
  public <T> T accept(BoolExprVisitor<T> tBoolExprVisitor) {
    return tBoolExprVisitor.visitFalseExpr(this);
  }
}
