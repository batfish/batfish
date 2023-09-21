package org.batfish.datamodel.bgp.community;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.JsonNode;
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
@JsonSubTypes({
  @JsonSubTypes.Type(value = StandardCommunity.class, name = "standard"),
  @JsonSubTypes.Type(value = ExtendedCommunity.class, name = "extended"),
  @JsonSubTypes.Type(value = LargeCommunity.class, name = "large")
})
@ParametersAreNonnullByDefault
public abstract class Community implements Serializable, Comparable<Community> {

  private @Nullable transient BigInteger _bigInt;

  @JsonCreator
  private static Community create(@Nullable JsonNode node) {
    checkArgument(node != null && !node.isNull(), "Invalid value for BGP community");
    if (node.isIntegralNumber()) {
      // Backwards compatible with previous long representation
      return StandardCommunity.of(node.longValue());
    }
    if (!node.isTextual()) {
      throw new IllegalArgumentException(
          String.format("Invalid value for BGP community: %s", node));
    }
    String str = node.textValue();
    return fromString(str);
  }

  /**
   * Parse a community from its {@link #toString()} representation
   *
   * <p>This method is also used by Jackson to deserialize a {@link Community} used as a Map key
   * (encoded as a String).
   */
  @JsonCreator
  private static @Nonnull Community fromString(String str) {
    // Try each possible type
    switch (str.split(":").length) {
      case 2:
        return StandardCommunity.parse(str);
      case 3:
        return ExtendedCommunity.parse(str);
      case 4:
        return LargeCommunity.parse(str);
      default:
        throw new IllegalArgumentException(
            String.format("Invalid value for BGP community: %s", str));
    }
  }

  /**
   * Whether this community is transitive (can traverse from autonomous system to autonomous system)
   */
  public abstract boolean isTransitive();

  public abstract <T> T accept(CommunityVisitor<T> visitor);

  /**
   * Return the community value as a {@link java.math.BigInteger} so it can be compared and ordered
   * deterministically regardless of community type
   */
  public final @Nonnull BigInteger asBigInt() {
    BigInteger bigInt = _bigInt;
    if (bigInt == null) {
      bigInt = asBigIntImpl();
      _bigInt = bigInt;
    }
    return bigInt;
  }

  /**
   * Return the community value as a {@link java.math.BigInteger} so it can be compared and ordered
   * deterministically regardless of community type
   */
  protected @Nonnull abstract BigInteger asBigIntImpl();

  /** Return a string representation of the community suitable for regex matching. */
  public @Nonnull abstract String matchString();

  /** Return a string representation of the community in canonical form. */
  @Override
  public @Nonnull abstract String toString();

  @Override
  public abstract boolean equals(@Nullable Object obj);

  @Override
  public abstract int hashCode();

  @Override
  public int compareTo(Community o) {
    return asBigInt().compareTo(o.asBigInt());
  }
}
