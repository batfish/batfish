package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.acl.GenericAclLineVisitor;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
/** A line in an {@link IpAccessList} */
public abstract class AclLine implements Serializable {
  protected static final String PROP_NAME = "name";
  protected static final String PROP_TRACE_ELEMENT = "traceElement";

  @Nullable protected final String _name;
  @Nullable protected final TraceElement _traceElement;

  AclLine(@Nullable String name) {
    _name = name;
    _traceElement = null;
  }

  AclLine(@Nullable String name, @Nullable TraceElement traceElement) {
    _name = name;
    _traceElement = traceElement;
  }

  /** The name of this line in the list */
  @JsonProperty(PROP_NAME)
  public final String getName() {
    return _name;
  }

  @JsonProperty(PROP_TRACE_ELEMENT)
  public final TraceElement getTraceElement() {
    return _traceElement;
  }

  public abstract <R> R accept(GenericAclLineVisitor<R> visitor);
}
