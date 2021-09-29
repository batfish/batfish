package org.batfish.datamodel.isp_configuration;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * Border interface information used to create ISPs. This specification is used for L3 interfaces
 * that connect directly to one ISP. In addition to being a simple L3 interface (e.g., Ethernet1),
 * the interface may be a port-channel or a sub-interface. The specification allows for multiple BGP
 * sessions to the same ISP (on the interface) but not to different ISPs.
 *
 * <p>New L1 links implied by the interface type will be created as part of this modeling, and ISP
 * node creation will fail if these L1 links conflict with existing L1 links.
 */
// TODO: add support for port channels and sub-interfaces in IspModelingUtils
// TODO: add support for multiple sessions to the same ISP in IspModelingUtils
public class BorderInterfaceInfo {
  private static final String PROP_BORDER_INTERFACE = "borderInterface";

  @Nonnull private final NodeInterfacePair _borderInterface;

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
  @Nonnull
  public NodeInterfacePair getBorderInterface() {
    return _borderInterface;
  }
}
