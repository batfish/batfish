package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.NavigableMap;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

public class RoutingPoliciesDiff extends ConfigDiffElement {

  @JsonCreator()
  public RoutingPoliciesDiff() {}

  public RoutingPoliciesDiff(
      NavigableMap<String, RoutingPolicy> before, NavigableMap<String, RoutingPolicy> after) {
    super(before, after, false);
  }

  @Override
  protected boolean skip(String name) {
    return name.startsWith("~") && name.endsWith("~");
  }
}
