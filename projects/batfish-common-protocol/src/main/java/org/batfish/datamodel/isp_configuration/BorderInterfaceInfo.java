package org.batfish.datamodel.isp_configuration;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * Border interface information used to create ISPs. This specification is used for layer-3
 * interfaces that are wired directly to one ISP, including physical interfaces (e.g., Ethernet1), a
 * aggregate interfaces (e.g., Port-Channel1), and sub-interface of a physical or aggregate
 * interface.
 *
 * <p>As {@link BorderInterfaceInfo} can only be used for directly-connected ISPs, multiple BGP
 * sessions on the interface or subinterfaces must be to the same directly-connected ISP.
 *
 * <p>Batfish will automatically create new layer-1 edges between the physical interfaces
 * corresponding to each {@link BorderInterfaceInfo} and its ISP. If any of these interfaces already
 * have a layer-1 edge, ISP modeling will fail for this {@link BorderInterfaceInfo}.
 *
 * <p>Interface names are case-insensitive, but full names must be used (not short forms).
 */
// TODO: check for layer-1 inconsistency and exclude this specification
// TODO: add support for port channels and sub-interfaces in IspModelingUtils
// TODO: add support for multiple sessions to the same ISP in IspModelingUtils
public class BorderInterfaceInfo {
  private static final String PROP_BORDER_INTERFACE = "borderInterface";

  private final @Nonnull NodeInterfacePair _borderInterface;

  public BorderInterfaceInfo(@Nonnull NodeInterfacePair borderInterface) {
    _borderInterface = borderInterface;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BorderInterfaceInfo)) {
      return false;
    }
    BorderInterfaceInfo that = (BorderInterfaceInfo) o;
    return Objects.equals(_borderInterface, that._borderInterface);
  }

  @Override
  public int hashCode() {
    return _borderInterface.hashCode();
  }

  @JsonCreator
  private static BorderInterfaceInfo jsonCreator(
      @JsonProperty(PROP_BORDER_INTERFACE) @Nullable NodeInterfacePair borderInterface) {
    checkArgument(borderInterface != null, "Missing %s", PROP_BORDER_INTERFACE);
    return new BorderInterfaceInfo(borderInterface);
  }

  @JsonProperty(PROP_BORDER_INTERFACE)
  public @Nonnull NodeInterfacePair getBorderInterface() {
    return _borderInterface;
  }
}
