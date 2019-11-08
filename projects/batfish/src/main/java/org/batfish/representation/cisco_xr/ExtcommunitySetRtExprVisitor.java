package org.batfish.representation.cisco_xr;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A visitor of {@link ExtcommunitySetRtExpr} that takes 1 generic argument and returns a generic
 * value.
 */
@ParametersAreNonnullByDefault
public interface ExtcommunitySetRtExprVisitor<T, U> {

  T visitExtcommunitySetRtReference(ExtcommunitySetRtReference extcommunitySetRtReference, U arg);

  T visitInlineExtcommunitySetRt(InlineExtcommunitySetRt inlineExtcommunitySetRt, U arg);
}
