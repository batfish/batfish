package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.Pair;

public class RoleEdge extends Pair<String, String> {

  private static final String PROP_ROLE1 = "role1";

  private static final String PROP_ROLE2 = "role2";

  private static final long serialVersionUID = 1L;

  @JsonCreator
  public RoleEdge(@JsonProperty(PROP_ROLE1) String role1, @JsonProperty(PROP_ROLE2) String role2) {
    super(role1, role2);
  }

  @JsonProperty(PROP_ROLE1)
  public String getRole1() {
    return _first;
  }

  @JsonProperty(PROP_ROLE2)
  public String getRole2() {
    return _second;
  }

  @Override
  public String toString() {
    return "<" + _first + " --> " + _second + ">";
  }
}
