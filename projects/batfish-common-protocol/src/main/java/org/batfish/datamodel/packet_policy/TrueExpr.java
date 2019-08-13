package org.batfish.datamodel.packet_policy;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nullable;

/** True boolean identity */
public final class TrueExpr implements BoolExpr {
  private static final TrueExpr INSTANCE = new TrueExpr();

  @JsonCreator
  private TrueExpr() {}

  public static TrueExpr instance() {
    return INSTANCE;
  }

  @Override
  public int hashCode() {
    return 0x6683f3a1;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return obj instanceof TrueExpr;
  }

  @Override
  public <T> T accept(BoolExprVisitor<T> tBoolExprVisitor) {
    return tBoolExprVisitor.visitTrueExpr(this);
  }
}
