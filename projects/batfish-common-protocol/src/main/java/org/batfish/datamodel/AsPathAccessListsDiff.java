package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.NavigableMap;

public class AsPathAccessListsDiff extends ConfigDiffElement {

  @JsonCreator()
  public AsPathAccessListsDiff() {}

  public AsPathAccessListsDiff(
      NavigableMap<String, AsPathAccessList> before, NavigableMap<String, AsPathAccessList> after) {
    super(before, after, false);
  }
}
