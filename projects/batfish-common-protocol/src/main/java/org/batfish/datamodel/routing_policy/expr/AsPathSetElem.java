package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class AsPathSetElem implements Serializable {

  @Override
  public abstract boolean equals(Object obj);

  @Override
  public abstract int hashCode();

  /**
   * Returns the regular expression associated with this element.
   *
   * <p>The resultant regular expression will be used to match against an AS Path as a string of
   * numbers, each preceded by a space. An empty AS Path will be represented as an empty string, and
   * the AS Path {@code 1 2 3} will be represented as {@code " 1 2 3"}.
   */
  public abstract String regex();
}
