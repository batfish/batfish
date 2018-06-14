package org.batfish.common.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.io.Serializable;

public abstract class ComparableStructure<KeyT extends Comparable<? super KeyT>>
    extends ReferenceCountedStructure implements Serializable {

  protected static final String PROP_NAME = "name";
  private static final long serialVersionUID = 1L;
  protected KeyT _key;

  @JsonCreator
  public ComparableStructure(@JsonProperty(PROP_NAME) KeyT name) {
    _key = name;
  }

  @JsonProperty(PROP_NAME)
  @JsonPropertyDescription("The name of this structure")
  public KeyT getName() {
    return _key;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "<" + _key + ">";
  }
}
