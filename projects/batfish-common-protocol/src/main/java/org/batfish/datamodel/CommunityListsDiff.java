package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.NavigableMap;

public class CommunityListsDiff extends ConfigDiffElement {

  @JsonCreator()
  public CommunityListsDiff() {}

  public CommunityListsDiff(
      NavigableMap<String, CommunityList> before, NavigableMap<String, CommunityList> after) {
    super(before, after, false);
  }
}
