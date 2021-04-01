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

  public void setComments(String comments) {
    _comments = comments;
  }

  public RouteMap(String name) {
    _name = name;

    _rules = new LinkedHashMap<>();
  }

  @Nonnull private String _name;
  @Nullable private String _comments;
  // Note: using LinkedHashMap here to preserve insertion order
  @Nonnull private final Map<String, RouteMapRule> _rules;
}
