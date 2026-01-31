package org.batfish.vendor.cisco_nxos.representation;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip6;

/** IPv6 HSRP group within {@link InterfaceHsrp} settings for an {@link Interface}. */
public final class HsrpGroupIpv6 extends HsrpGroup {

  public HsrpGroupIpv6(int group) {
    super(group);
    _ipSecondaries = new HashSet<>();
  }

  public @Nullable Ip6 getIp() {
    return _ip;
  }

  public void setIp(@Nullable Ip6 ip) {
    _ip = ip;
  }

  public @Nonnull Set<Ip6> getIpSecondaries() {
    return _ipSecondaries;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private @Nullable Ip6 _ip;
  private final @Nonnull Set<Ip6> _ipSecondaries;
}
