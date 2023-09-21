package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** XR datamodel component containing bridge-domain configuration */
@ParametersAreNonnullByDefault
public class BridgeDomain implements Serializable {
  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Set<String> getInterfaces() {
    return _interfaces;
  }

  public @Nullable String getRoutedInterface() {
    return _routedInterface;
  }

  public void setRoutedInterface(String routedInterface) {
    _routedInterface = routedInterface;
  }

  public BridgeDomain(String name) {
    _name = name;
    _interfaces = new HashSet<>();
  }

  private final @Nonnull String _name;
  private final @Nonnull Set<String> _interfaces;
  private @Nullable String _routedInterface;
}
