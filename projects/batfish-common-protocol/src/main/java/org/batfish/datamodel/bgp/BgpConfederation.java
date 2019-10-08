package org.batfish.datamodel.bgp;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents configuration for a BGP confederation.
 *
 * <p>See <a href="https://tools.ietf.org/html/rfc5065"></a>
 */
@ParametersAreNonnullByDefault
public final class BgpConfederation implements Serializable {
  private final long _id;
  @Nonnull private final SortedSet<Long> _members;

  /**
   * Create new confederation config.
   *
   * @param id externally visible autonomous system number
   * @param members Set of autonomous system numbers visible only within a BGP confederation
   */
  public BgpConfederation(long id, Set<Long> members) {
    checkArgument(!members.isEmpty(), "BGP confederation without members is not allowed");
    _id = id;
    _members = ImmutableSortedSet.copyOf(members);
  }

  /**
   * An externally visible autonomous system number that identifies a BGP confederation as a whole.
   */
  @JsonProperty(PROP_ID)
  public long getId() {
    return _id;
  }

  /**
   * Set of autonomous system numbers visible only within a BGP confederation, and used to represent
   * a Member-AS within that confederation.
   */
  @Nonnull
  @JsonProperty(PROP_MEMBERS)
  public SortedSet<Long> getMembers() {
    return _members;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BgpConfederation)) {
      return false;
    }
    BgpConfederation that = (BgpConfederation) o;
    return _id == that._id && _members.equals(that._members);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_id, _members);
  }

  private static final String PROP_ID = "id";
  private static final String PROP_MEMBERS = "members";

  @JsonCreator
  private static BgpConfederation create(
      @Nullable @JsonProperty(PROP_ID) Long id,
      @Nullable @JsonProperty(PROP_MEMBERS) Set<Long> members) {
    checkArgument(id != null, "Missing %s", PROP_ID);
    checkArgument(members != null, "Missing %s", PROP_ID);
    return new BgpConfederation(id, members);
  }
}
