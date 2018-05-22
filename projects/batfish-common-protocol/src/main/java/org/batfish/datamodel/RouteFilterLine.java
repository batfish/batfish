package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.base.MoreObjects;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.io.Serializable;

@JsonSchemaDescription("A line in a RouteFilterList")
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
    if (_action != other._action) {
      return false;
    }
    if (!_lengthRange.equals(other._lengthRange)) {
      return false;
    }
    if (!_ipWildcard.equals(other._ipWildcard)) {
      return false;
    }
    return true;
  }

  @JsonProperty(PROP_ACTION)
  @JsonPropertyDescription(
      "The action the underlying access-list will take when this line matches an IPV4 route.")
  public LineAction getAction() {
    return _action;
  }

  @JsonProperty(PROP_LENGTH_RANGE)
  @JsonPropertyDescription("The range of acceptable prefix-lengths for a route.")
  public SubRange getLengthRange() {
    return _lengthRange;
  }

  @JsonProperty(PROP_IP_WILDCARD)
  @JsonPropertyDescription(
      "The bits against which to compare a route's prefix. The mask of this IP Wildcard determines "
          + "which bits must match")
  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _action.ordinal();
    result = prime * result + _lengthRange.hashCode();
    result = prime * result + _ipWildcard.hashCode();
    return result;
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
