package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;

/** A line in a {@link RouteFilterList}. */
public class RouteFilterLine implements Serializable {

  private static final String PROP_ACTION = "action";

  private static final String PROP_LENGTH_RANGE = "lengthRange";

  private static final String PROP_IP_WILDCARD = "ipWildcard";

  private static final long serialVersionUID = 1L;

  private final LineAction _action;

  private final IpWildcard _ipWildcard;

  private final SubRange _lengthRange;

  @JsonCreator
  public RouteFilterLine(
      @JsonProperty(PROP_ACTION) LineAction action,
      @JsonProperty(PROP_IP_WILDCARD) IpWildcard ipWildcard,
      @JsonProperty(PROP_LENGTH_RANGE) SubRange lengthRange) {
    _action = action;
    _ipWildcard = ipWildcard;
    _lengthRange = lengthRange;
  }

  public RouteFilterLine(LineAction action, Prefix prefix, SubRange lengthRange) {
    _action = action;
    _ipWildcard = new IpWildcard(prefix);
    _lengthRange = lengthRange;
  }

  public RouteFilterLine(LineAction action, PrefixRange prefixRange) {
    this(action, new IpWildcard(prefixRange.getPrefix()), prefixRange.getLengthRange());
  }

  @Override
  public boolean equals(Object o) {
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

  public String toCompactString() {
    StringBuilder sb = new StringBuilder();
    sb.append(_action + " ");
    sb.append(_ipWildcard + " ");
    sb.append(_lengthRange + " ");
    return sb.toString();
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
