package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.io.Serializable;

@JsonSchemaDescription("A line in an AsPathAccessList")
public final class AsPathAccessListLine implements Serializable, Comparable<AsPathAccessListLine> {

  private static final long serialVersionUID = 1L;

  private LineAction _action;

  private String _regex;

  @Override
  public int compareTo(AsPathAccessListLine rhs) {
    int ret = _regex.compareTo(rhs._regex);
    return ret;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof AsPathAccessListLine)) {
      return false;
    }
    AsPathAccessListLine other = (AsPathAccessListLine) o;
    if (_action != other._action) {
      return false;
    }
    if (!_regex.equals(other._regex)) {
      return false;
    }
    return true;
  }

  @JsonPropertyDescription(
      "The action the underlying access-list will take when this line matches a route.")
  public LineAction getAction() {
    return _action;
  }

  @JsonPropertyDescription("The regex against which a route's AS-path will be compared")
  public String getRegex() {
    return _regex;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _action.ordinal();
    result = prime * result + _regex.hashCode();
    return result;
  }

  public void setAction(LineAction action) {
    _action = action;
  }

  public void setRegex(String regex) {
    _regex = regex;
  }
}
