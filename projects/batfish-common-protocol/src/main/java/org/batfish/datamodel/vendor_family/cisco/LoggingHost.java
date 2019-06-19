package org.batfish.datamodel.vendor_family.cisco;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.util.ComparableStructure;

public class LoggingHost extends ComparableStructure<String> {

  private static final long serialVersionUID = 1L;

  @JsonCreator
  public LoggingHost(@JsonProperty(PROP_NAME) String name) {
    super(name);
  }
}
