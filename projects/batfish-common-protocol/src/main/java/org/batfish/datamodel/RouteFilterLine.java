package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.io.Serializable;

@JsonSchemaDescription("A line in an RouteFilterList")
public class RouteFilterLine implements Serializable {

  private static final String PROP_ACTION = "action";

  private static final String PROP_LENGTH_RANGE = "lengthRange";

  private static final String PROP_PREFIX = "prefix";

  private static final long serialVersionUID = 1L;

  private final LineAction _action;

  private final SubRange _lengthRange;

  private final Prefix _prefix;

  @JsonCreator
  public RouteFilterLine(
      @JsonProperty(PROP_ACTION) LineAction action,
      @JsonProperty(PROP_PREFIX) Prefix prefix,
      @JsonProperty(PROP_LENGTH_RANGE) SubRange lengthRange) {
    _action = action;
    _prefix = prefix;
    _lengthRange = lengthRange;
  }

  public RouteFilterLine(LineAction action, PrefixRange prefixRange) {
    this(action, prefixRange.getPrefix(), prefixRange.getLengthRange());
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
    if (!_prefix.equals(other._prefix)) {
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

  @JsonProperty(PROP_PREFIX)
  @JsonPropertyDescription(
      "The bits against which to compare a route's prefix. The length of this prefix is used to "
          + "determine how many leading bits must match.")
  public Prefix getPrefix() {
    return _prefix;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _action.ordinal();
    result = prime * result + _lengthRange.hashCode();
    result = prime * result + _prefix.hashCode();
    return result;
  }

  public String toCompactString() {
    StringBuilder sb = new StringBuilder();
    sb.append(_action + " ");
    sb.append(_prefix + " ");
    sb.append(_lengthRange + " ");
    return sb.toString();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{ ");
    sb.append("Action=" + _action + " ");
    sb.append("Prefix=" + _prefix + " ");
    sb.append("LengthRange=" + _lengthRange + " ");
    sb.append("}");
    return sb.toString();
  }
}
