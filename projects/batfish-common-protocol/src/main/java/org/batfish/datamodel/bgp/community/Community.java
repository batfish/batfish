package org.batfish.datamodel.bgp.community;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.math.BigInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents a BGP community value, which could be <a
 * href="https://tools.ietf.org/html/rfc1997">standard</a>, <a
 * href="https://tools.ietf.org/html/rfc4360">extended</a> or <a
 * href="https://tools.ietf.org/html/rfc8092">large</a>
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    // Keep backwards-compatible with previous community implementation
    defaultImpl = StandardCommunity.class)
@JsonSubTypes({
  @JsonSubTypes.Type(value = StandardCommunity.class, name = "standard"),
  @JsonSubTypes.Type(value = ExtendedCommunity.class, name = "extended"),
  @JsonSubTypes.Type(value = LargeCommunity.class, name = "large")
})
@ParametersAreNonnullByDefault
public abstract class Community implements Serializable, Comparable<Community> {

  private static final long serialVersionUID = 1L;

  /**
   * Whether this community is transitive (can traverse from autonomous system to autonomous system)
   */
  public abstract boolean isTransitive();

  /**
   * Return the community value as a {@link java.math.BigInteger} so it can be compared and ordered
   * deterministically regardless of community type
   */
  @Nonnull
  public abstract BigInteger asBigInt();

  /** Return a string representation of the community suitable for regex matching. */
  @Nonnull
  public abstract String matchString();

  /** Return a string representation of the community in canonical form. */
  @Override
  @Nonnull
  public abstract String toString();

  @Override
  public abstract boolean equals(@Nullable Object obj);

  @Override
  public abstract int hashCode();

  @Override
  public int compareTo(Community o) {
    return asBigInt().compareTo(o.asBigInt());
  }
}
