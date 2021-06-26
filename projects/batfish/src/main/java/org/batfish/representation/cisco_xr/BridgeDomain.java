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
  @Nonnull
  public String getName() {
    return _name;
  }

  @Nonnull
  public Set<String> getInterfaces() {
    return _interfaces;
  }

  @Nullable
  public String getRoutedInterface() {
    return _routedInterface;
  }

  public void setRoutedInterface(String routedInterface) {
    _routedInterface = routedInterface;
  }

  public BridgeDomain(String name) {
    _name = name;
    _interfaces = new HashSet<>();
  }

  @Nonnull private final String _name;
  @Nonnull private final Set<String> _interfaces;
  @Nullable private String _routedInterface;
}
