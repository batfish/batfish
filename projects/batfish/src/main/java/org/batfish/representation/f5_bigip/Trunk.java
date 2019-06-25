package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** An aggregated Layer-1 interface composed of one or more layer-1 port {@link Interface}s */
@ParametersAreNonnullByDefault
public final class Trunk implements Serializable {

  private final @Nonnull Set<String> _interfaces;

  private final @Nonnull String _name;

  public Trunk(String name) {
    _name = name;
    _interfaces = new HashSet<>();
  }

  public @Nonnull Set<String> getInterfaces() {
    return _interfaces;
  }

  public @Nonnull String getName() {
    return _name;
  }
}
