package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.base.Strings;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.util.Objects;

@JsonSchemaDescription("A line in an IpAccessList")
public final class IpAccessListLine extends HeaderSpace {

  private static final long serialVersionUID = 1L;

  private LineAction _action;

  private String _name;

  private String _inInterfaceName;

  private String _outInterfaceName;

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    IpAccessListLine other = (IpAccessListLine) obj;
    if (!super.equals(obj)) {
      return false;
    }
    if (_action != other._action) {
      return false;
    }
    if (!Objects.equals(_inInterfaceName, other._inInterfaceName)) {
      return false;
    }
    if (!Objects.equals(_outInterfaceName, other._outInterfaceName)) {
      return false;
    }
    return true;
  }

  @JsonPropertyDescription(
      "The action the underlying access-list will take when this line matches an IPV4 packet.")
  public LineAction getAction() {
    return _action;
  }

  @JsonSchemaDescription("The input interface this rule is associated with")
  public String getInInterfaceName() {
    return _inInterfaceName;
  }

  @JsonSchemaDescription("The name of this line in the list")
  public String getName() {
    return _name;
  }

  @JsonSchemaDescription("The output interface this rule is associated with")
  public String getOutInterfaceName() {
    return _outInterfaceName;
  }

  @Override
  public int hashCode() {
    // TODO: implement better hashcode
    return 0;
  }

  public void setAction(LineAction action) {
    _action = action;
  }

  public void setInInterfaceName(String inInterfaceName) {
    _inInterfaceName = inInterfaceName;
  }

  public void setName(String name) {
    _name = name;
  }

  public void setOutInterfaceName(String outInterfaceName) {
    _outInterfaceName = outInterfaceName;
  }

  @Override
  public String toString() {
    return "[Action:" + _action + ", Base: " + super.toString() + "]";
  }
}
