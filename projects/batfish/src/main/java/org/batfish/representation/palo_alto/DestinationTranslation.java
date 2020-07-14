package org.batfish.representation.palo_alto;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.representation.palo_alto.RuleEndpoint.Type;

/** Represents destination-translation clause in Palo Alto NAT rule */
public class DestinationTranslation implements Serializable {

  @Nullable private RuleEndpoint _translatedAddress;
  @Nullable private Integer _translatedPort;

  @Nullable
  public RuleEndpoint getTranslatedAddress() {
    return _translatedAddress;
  }

  @Nullable
  public Integer getTranslatedPort() {
    return _translatedPort;
  }

  public void setTranslatedAddress(RuleEndpoint address) {
    checkArgument(address.getType() != Type.Any, "Cannot use value any for translated-address");
    _translatedAddress = address;
  }

  public void setTranslatedPort(int port) {
    _translatedPort = port;
  }
}
