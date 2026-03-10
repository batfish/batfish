package org.batfish.datamodel.bgp;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.LongSpace;

/**
 * Represents configuration for a BGP confederation.
 *
 * <p>See <a href="https://tools.ietf.org/html/rfc5065"></a>
 */
@ParametersAreNonnullByDefault
public final class BgpConfederation implements Serializable {
  private final long _id;
  private final @Nonnull LongSpace _members;

  /**
   * @see #BgpConfederation(long, Set)
   */
  public BgpConfederation(long id, Set<Long> members) {
    checkArgument(!members.isEmpty(), "BGP confederation without members is not allowed");
    _id = id;
    LongSpace.Builder asSpace = LongSpace.builder();
    members.forEach(asSpace::including);
    _members = asSpace.build();
  }

  /**
   * Create new confederation config.
   *
   * @param id externally visible autonomous system number
   * @param members Set of autonomous system numbers visible only within a BGP confederation
   */
  public BgpConfederation(long id, LongSpace members) {
    checkArgument(!members.isEmpty(), "BGP confederation without members is not allowed");
    _id = id;
    _members = members;
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
  @JsonProperty(PROP_MEMBERS)
  public @Nonnull LongSpace getMembers() {
    return _members;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof BgpConfederation)) {
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
      @JsonProperty(PROP_ID) @Nullable Long id,
      @JsonProperty(PROP_MEMBERS) @Nullable LongSpace members) {
    checkArgument(id != null, "Missing %s", PROP_ID);
    checkArgument(members != null, "Missing %s", PROP_ID);
    return new BgpConfederation(id, members);
  }
}
