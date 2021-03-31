package org.batfish.representation.fortios;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** FortiOS datamodel component containing access-list configuration */
public class AccessList implements Serializable {

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nullable
  public String getComments() {
    return _comments;
  }

  @Nonnull
  public Map<String, AccessListRule> getRules() {
    return _rules;
  }

  public void setComments(String comments) {
    _comments = comments;
  }

  public AccessList(String name) {
    _name = name;

    _rules = new LinkedHashMap<>();
  }

  @Nonnull private final String _name;
  @Nullable private String _comments;
  // Note: this is a LinkedHashMaps to preserve insertion order
  @Nonnull private final Map<String, AccessListRule> _rules;
}
