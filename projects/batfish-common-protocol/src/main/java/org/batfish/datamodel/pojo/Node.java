package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.util.ComparableStructure;

public class Node extends ComparableStructure<String> {

  @JsonCreator
  public Node(@JsonProperty(PROP_NAME) String name) {
    super(name);
  }

  /** */
  private static final long serialVersionUID = 1L;
}
