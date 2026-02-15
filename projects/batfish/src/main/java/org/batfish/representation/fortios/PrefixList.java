package org.batfish.representation.fortios;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** FortiOS datamodel component containing prefix-list configuration */
public class PrefixList implements Serializable {

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getComments() {
    return _comments;
  }

  public @Nonnull Map<String, PrefixListRule> getRules() {
    return _rules;
  }

  public void setComments(String comments) {
    _comments = comments;
  }

  public PrefixList(String name) {
    _name = name;
    _rules = new LinkedHashMap<>();
  }

  private final @Nonnull String _name;
  private @Nullable String _comments;
  // Note: this is a LinkedHashMap to preserve insertion order
  private final @Nonnull Map<String, PrefixListRule> _rules;
}
