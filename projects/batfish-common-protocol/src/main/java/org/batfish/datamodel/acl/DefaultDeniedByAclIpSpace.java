package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import javax.annotation.Nonnull;

public class DefaultDeniedByAclIpSpace implements TraceEvent {

  private static final String PROP_NAME = "name";

  private static final long serialVersionUID = 1L;

  private final String _name;

  @JsonCreator
  public DefaultDeniedByAclIpSpace(@JsonProperty(PROP_NAME) @Nonnull String name) {
    _name = name;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DefaultDeniedByAclIpSpace)) {
      return false;
    }
    DefaultDeniedByAclIpSpace rhs = (DefaultDeniedByAclIpSpace) obj;
    return _name.equals(rhs._name);
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add(PROP_NAME, _name).toString();
  }
}
