package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Settings at the {@code router hsrp} level. */
@ParametersAreNonnullByDefault
public final class Hsrp implements Serializable {
  public Hsrp() {
    _interfaces = new HashMap<>();
  }

  public @Nullable HsrpInterface getInterface(String name) {
    return _interfaces.get(name);
  }

  public @Nonnull HsrpInterface getOrCreateInterface(String name) {
    return _interfaces.computeIfAbsent(name, HsrpInterface::new);
  }

  public @Nonnull Map<String, HsrpInterface> getInterfaces() {
    return Collections.unmodifiableMap(_interfaces);
  }

  private final Map<String, HsrpInterface> _interfaces;
}
