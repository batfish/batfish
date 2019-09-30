package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A named community structure in the Juniper vendor model composed of one or more {@link
 * CommunityMember}s.
 *
 * <p>
 *
 * <ul>
 *   <li>When setting communities, interpreted as set of individual communities. In this case, only
 *       non-regex members are considered.
 *   <li>When matching communities, interpreted as a conjunction of required match conditions: each
 *       member must match at least one community from the route to be matched.
 * </ul>
 */
@ParametersAreNonnullByDefault
public final class NamedCommunity implements Serializable {

  public NamedCommunity(String name) {
    _name = name;
    _members = new HashSet<>();
  }

  public boolean getInvertMatch() {
    return _invertMatch;
  }

  public void setInvertMatch(boolean invertMatch) {
    _invertMatch = invertMatch;
  }

  public @Nonnull Set<CommunityMember> getMembers() {
    return _members;
  }

  public @Nonnull String getName() {
    return _name;
  }

  private boolean _invertMatch;
  private final @Nonnull Set<CommunityMember> _members;
  private final @Nonnull String _name;
}
