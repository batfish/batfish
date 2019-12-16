package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.acl.GenericAclLineVisitor;

/** A line in an {@link IpAccessList} */
public abstract class AbstractAclLine implements Serializable {
  protected static final String PROP_NAME = "name";

  @Nullable protected final String _name;

  AbstractAclLine(@Nullable String name) {
    _name = name;
  }

  /** The name of this line in the list */
  @JsonProperty(PROP_NAME)
  public final String getName() {
    return _name;
  }

  public abstract <R> R accept(GenericAclLineVisitor<R> visitor);
}
