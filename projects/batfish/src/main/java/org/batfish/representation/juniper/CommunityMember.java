package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A member of a {@link NamedCommunity} structure. */
@ParametersAreNonnullByDefault
public interface CommunityMember extends Serializable {

  <T> T accept(CommunityMemberVisitor<T> visitor);
}
