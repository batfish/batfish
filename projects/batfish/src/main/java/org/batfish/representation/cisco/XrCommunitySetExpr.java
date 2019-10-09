package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An expression representing a {@link XrCommunitySet}. */
@ParametersAreNonnullByDefault
public interface XrCommunitySetExpr extends Serializable {

  <T, U> T accept(XrCommunitySetExprVisitor<T, U> visitor, U arg);
}
