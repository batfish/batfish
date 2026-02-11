package org.batfish.representation.fortios;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** FortiOS datamodel component containing route-map configuration */
public class RouteMap implements Serializable {

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getComments() {
    return _comments;
  }

  public @Nonnull Map<String, RouteMapRule> getRules() {
    return _rules;
  }

  public void setRules(Map<String, RouteMapRule> rules) {
    _rules.clear();
    _rules.putAll(rules);
  }

  public void setComments(String comments) {
    _comments = comments;
  }

  public RouteMap(String name) {
    _name = name;

    _rules = new LinkedHashMap<>();
  }

  private final @Nonnull String _name;
  private @Nullable String _comments;
  // Note: using LinkedHashMap here to preserve insertion order
  private final @Nonnull Map<String, RouteMapRule> _rules;
}
