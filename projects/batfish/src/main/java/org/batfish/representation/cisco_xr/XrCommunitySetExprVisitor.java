package org.batfish.representation.cisco_xr;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A visitor of {@link XrCommunitySetExpr} that takes 1 generic argument and returns a generic
 * value.
 */
@ParametersAreNonnullByDefault
public interface XrCommunitySetExprVisitor<T, U> {

  T visitCommunitySetReference(XrCommunitySetReference communitySetReference, U arg);

  T visitInlineCommunitySet(XrInlineCommunitySet inlineCommunitySet, U arg);
}
