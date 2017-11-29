package org.batfish.common.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class DefinedStructure<KeyT extends Comparable<KeyT>> extends ComparableStructure<KeyT> {

  /** */
  private static final long serialVersionUID = 1L;

  private final int _definitionLine;

  public DefinedStructure(@JsonProperty(PROP_NAME) KeyT name, int definitionLine) {
    super(name);
    _definitionLine = definitionLine;
  }

  @JsonIgnore
  public final int getDefinitionLine() {
    return _definitionLine;
  }
}
