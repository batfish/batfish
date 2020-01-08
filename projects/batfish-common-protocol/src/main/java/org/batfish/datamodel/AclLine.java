package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.acl.GenericAclLineVisitor;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
/** A line in an {@link IpAccessList} */
public abstract class AclLine implements Serializable {
  protected static final String PROP_NAME = "name";

  @Nullable protected final TraceElement _name;

  AclLine(@Nullable String name) {
    _name = name == null ? null : TraceElement.of(name);
  }

  AclLine(@Nullable TraceElement name) {
    _name = name;
  }

  /** The name of this line in the list */
  @JsonProperty(PROP_NAME)
  public final TraceElement getName() {
    return _name;
  }

  @JsonIgnore
  public final String getNameAsString() {
    return _name == null ? null : _name.toString();
  }

  public abstract <R> R accept(GenericAclLineVisitor<R> visitor);
}
