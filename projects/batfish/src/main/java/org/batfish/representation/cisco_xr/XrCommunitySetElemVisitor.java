package org.batfish.representation.cisco_xr;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A visitor of {@link XrCommunitySetElem} that takes 1 generic argument and returns a generic
 * value.
 */
@ParametersAreNonnullByDefault
public interface XrCommunitySetElemVisitor<T, U> {

  T visitCommunitySetHighLowRangeExprs(XrCommunitySetHighLowRangeExprs highLowRangeExprs, U arg);

  T visitCommunitySetIosRegex(XrCommunitySetIosRegex communitySetIosRegex, U arg);
}
