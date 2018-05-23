package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;

public class PermittedByNamedIpSpace implements TraceEvent {

  private static final String PROP_DESCRIPTION = "description";

  private static final String PROP_NAME = "name";

  private static final long serialVersionUID = 1L;

  private final String _description;

  private final String _name;

  @JsonCreator
  public PermittedByNamedIpSpace(
      @JsonProperty(PROP_NAME) @Nonnull String name,
      @JsonProperty(PROP_DESCRIPTION) @Nonnull String description) {
    _name = name;
    _description = description;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PermittedByNamedIpSpace)) {
      return false;
    }
    PermittedByNamedIpSpace rhs = (PermittedByNamedIpSpace) obj;
    return _description.equals(rhs._description) && _name.equals(rhs._name);
  }

  @JsonProperty(PROP_DESCRIPTION)
  public @Nonnull String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_description, _name);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add(PROP_NAME, _name)
        .add(PROP_DESCRIPTION, _description)
        .toString();
  }
}
