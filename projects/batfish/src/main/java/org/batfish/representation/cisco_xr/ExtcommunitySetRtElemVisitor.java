package org.batfish.representation.cisco_xr;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A visitor of {@link ExtcommunitySetRtElem} that takes 1 generic argument and returns a generic
 * value.
 */
@ParametersAreNonnullByDefault
public interface ExtcommunitySetRtElemVisitor<T, U> {

  T visitExtcommunitySetRtElemAsDotColon(
      ExtcommunitySetRtElemAsDotColon extcommunitySetRtElemAsDotColon, U arg);

  T visitExtcommunitySetRtElemAsColon(
      ExtcommunitySetRtElemAsColon extcommunitySetRtElemAsColon, U arg);
}
