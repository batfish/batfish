package org.batfish.datamodel.isp_configuration;

import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.collections.NodeInterfacePair;

public class BorderInterfaceInfo {
  private static final String PROP_BORDER_INTERFACE = "borderInterface";

  @Nonnull private final NodeInterfacePair _borderInterface;

  public BorderInterfaceInfo(@Nonnull NodeInterfacePair borderInterface) {
    _borderInterface = borderInterface;
  }

  @JsonCreator
  private static BorderInterfaceInfo jsonCreator(
      @JsonProperty(PROP_BORDER_INTERFACE) @Nullable NodeInterfacePair borderInterface) {
    checkState(Objects.nonNull(borderInterface), "borderInterface should not be null");
    return new BorderInterfaceInfo(borderInterface);
  }

  @JsonProperty(PROP_BORDER_INTERFACE)
  public NodeInterfacePair getBorderInterface() {
    return _borderInterface;
  }
}
