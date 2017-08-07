package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.Pair;

public class RoleEdge extends Pair<String, String> {

  private static final String ROLE1_VAR = "role1";

  private static final String ROLE2_VAR = "role2";

  private static final long serialVersionUID = 1L;

  @JsonCreator
  public RoleEdge(@JsonProperty(ROLE1_VAR) String role1, @JsonProperty(ROLE2_VAR) String role2) {
    super(role1, role2);
  }

  @JsonProperty(ROLE1_VAR)
  public String getRole1() {
    return _first;
  }

  @JsonProperty(ROLE2_VAR)
  public String getRole2() {
    return _second;
  }

  @Override
  public String toString() {
    return "<" + _first + " --> " + _second + ">";
  }
}
