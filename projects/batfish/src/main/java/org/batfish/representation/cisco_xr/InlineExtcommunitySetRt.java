package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** An inline {@link ExtcommunitySetRtExpr} defined within a {@link RoutePolicy}. */
@ParametersAreNonnullByDefault
public class InlineExtcommunitySetRt implements ExtcommunitySetRtExpr {

  public InlineExtcommunitySetRt(ExtcommunitySetRt extcommunitySetRt) {
    _extcommunitySetRt = extcommunitySetRt;
  }

  @Override
  public <T, U> T accept(ExtcommunitySetRtExprVisitor<T, U> visitor, U arg) {
    return visitor.visitInlineExtcommunitySetRt(this, arg);
  }

  public @Nonnull ExtcommunitySetRt getExtcommunitySetRt() {
    return _extcommunitySetRt;
  }

  private final @Nonnull ExtcommunitySetRt _extcommunitySetRt;
}
