package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Resolution settings for a routing instance. */
@ParametersAreNonnullByDefault
public final class Resolution implements Serializable {

  public Resolution() {
    _ribs = new HashMap<>();
  }

  public @Nonnull ResolutionRib getOrCreateRib(String name) {
    return _ribs.computeIfAbsent(name, ResolutionRib::new);
  }

  public boolean getPreserveNexthopHierarchy() {
    return _preserveNexthopHierarchy;
  }

  public @Nonnull Map<String, ResolutionRib> getRibs() {
    return _ribs;
  }

  public void setPreserveNexthopHierarchy(boolean preserveNexthopHierarchy) {
    _preserveNexthopHierarchy = preserveNexthopHierarchy;
  }

  private boolean _preserveNexthopHierarchy;
  private final @Nonnull Map<String, ResolutionRib> _ribs;
}
