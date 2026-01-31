package org.batfish.vendor.cisco_nxos.representation;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** IPv4 HSRP group within {@link InterfaceHsrp} settings for an {@link Interface}. */
public final class HsrpGroupIpv4 extends HsrpGroup {

  public HsrpGroupIpv4(int group) {
    super(group);
    _ipSecondaries = new HashSet<>();
  }

  public @Nullable Ip getIp() {
    return _ip;
  }

  public void setIp(@Nullable Ip ip) {
    _ip = ip;
  }

  public @Nonnull Set<Ip> getIpSecondaries() {
    return _ipSecondaries;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private @Nullable Ip _ip;
  private final @Nonnull Set<Ip> _ipSecondaries;
}
