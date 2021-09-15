package org.batfish.vendor.check_point_management;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.Serializable;
import org.batfish.datamodel.Ip;

/** Source to hide behind in a NAT hide rule. May be an IP or gateway. */
public abstract class NatHideBehind implements Serializable {
  protected static final String NAME_GATEWAY = "gateway";

  protected NatHideBehind() {}

  abstract <T> T accept(NatHideBehindVisitor<T> visitor);

  @JsonCreator
  private static NatHideBehind create(String jsonText) {
    if (jsonText.equals(NAME_GATEWAY)) {
      return NatHideBehindGateway.INSTANCE;
    }
    return new NatHideBehindIp(Ip.parse(jsonText));
  }
}
