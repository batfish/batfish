package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.NavigableMap;

public class InterfacesDiff extends ConfigDiffElement {

  @JsonCreator()
  public InterfacesDiff() {}

  public InterfacesDiff(
      NavigableMap<String, Interface> before, NavigableMap<String, Interface> after) {
    super(before, after, true);
  }
}
