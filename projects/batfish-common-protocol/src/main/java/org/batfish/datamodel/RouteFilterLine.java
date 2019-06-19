package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A line in a {@link RouteFilterList}. */
@ParametersAreNonnullByDefault
public final class RouteFilterLine implements Serializable {
  private static final String PROP_ACTION = "action";
  private static final String PROP_LENGTH_RANGE = "lengthRange";
  private static final String PROP_IP_WILDCARD = "ipWildcard";

  private static final long serialVersionUID = 1L;

  private final LineAction _action;
  private final IpWildcard _ipWildcard;
  private final SubRange _lengthRange;

  /** Route filter line that permits all routes */
  public static final RouteFilterLine PERMIT_ALL =
      new RouteFilterLine(
          LineAction.PERMIT, Prefix.ZERO, new SubRange(0, Prefix.MAX_PREFIX_LENGTH));

  @JsonCreator
  private static RouteFilterLine create(
      @Nullable @JsonProperty(PROP_ACTION) LineAction action,
      @Nullable @JsonProperty(PROP_IP_WILDCARD) IpWildcard ipWildcard,
      @Nullable @JsonProperty(PROP_LENGTH_RANGE) SubRange lengthRange) {
    checkArgument(action != null, "% is missing", PROP_ACTION);
    checkArgument(ipWildcard != null, "% is missing", PROP_IP_WILDCARD);
    checkArgument(lengthRange != null, "% is missing", PROP_LENGTH_RANGE);
    return new RouteFilterLine(action, ipWildcard, lengthRange);
  }

  public RouteFilterLine(LineAction action, IpWildcard ipWildcard, SubRange lengthRange) {
    _action = action;
    _ipWildcard = ipWildcard;
    _lengthRange = lengthRange;
  }

  public RouteFilterLine(LineAction action, Prefix prefix, SubRange lengthRange) {
    _action = action;
    _ipWildcard = IpWildcard.create(prefix);
    _lengthRange = lengthRange;
  }

  public RouteFilterLine(LineAction action, PrefixRange prefixRange) {
    this(action, IpWildcard.create(prefixRange.getPrefix()), prefixRange.getLengthRange());
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof RouteFilterLine)) {
      return false;
    }

    RouteFilterLine other = (RouteFilterLine) o;
    return Objects.equals(_action, other._action)
        && Objects.equals(_lengthRange, other._lengthRange)
        && Objects.equals(_ipWildcard, other._ipWildcard);
  }

  /** The action the underlying access-list will take when this line matches an IPV4 route. */
  @JsonProperty(PROP_ACTION)
  public LineAction getAction() {
    return _action;
  }

  /** The range of acceptable prefix-lengths for a route. */
  @JsonProperty(PROP_LENGTH_RANGE)
  public SubRange getLengthRange() {
    return _lengthRange;
  }

  /**
   * The bits against which to compare a route's prefix. The mask of this IP Wildcard determines
   * which bits must match.
   */
  @JsonProperty(PROP_IP_WILDCARD)
  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_action.ordinal(), _lengthRange, _ipWildcard);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("Action", _action)
        .add("IpWildCard", _ipWildcard)
        .add("LengthRange", _lengthRange)
        .toString();
  }
}
