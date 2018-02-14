package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.util.Set;

@JsonSchemaDescription("A line in an IpAccessList")
public final class IpAccessListLine extends HeaderSpace {

  public static class Builder {

    private LineAction _action;

    private Set<IpWildcard> _dstIps;

    private String _name;

    private Builder() {}

    public IpAccessListLine build() {
      IpAccessListLine line = new IpAccessListLine();
      line.setDstIps(ImmutableSortedSet.copyOf(_dstIps));
      line.setAction(_action);
      line.setName(_name);
      return line;
    }

    public Builder setDstIps(Iterable<IpWildcard> dstIps) {
      _dstIps = ImmutableSet.copyOf(dstIps);
      return this;
    }
  }

  private static final long serialVersionUID = 1L;

  public static Builder builder() {
    return new Builder();
  }

  private LineAction _action;

  private String _name;

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
    return true;
  }

  @JsonPropertyDescription(
      "The action the underlying access-list will take when this line matches an IPV4 packet.")
  public LineAction getAction() {
    return _action;
  }

  @JsonSchemaDescription("The name of this line in the list")
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    // TODO: implement better hashcode
    return 0;
  }

  public void setAction(LineAction action) {
    _action = action;
  }

  public void setName(String name) {
    _name = name;
  }

  @Override
  public String toString() {
    return "[Action:" + _action + ", Base: " + super.toString() + "]";
  }
}
