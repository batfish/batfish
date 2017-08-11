package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.util.ComparableStructure;

public class SnmpHost extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  public SnmpHost(@JsonProperty(PROP_NAME) String name) {
    super(name);
  }
}
