package org.batfish.vendor.a10.representation;

import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/** Definition of a vrrp-a fail-over-policy-template. */
public final class VrrpAFailOverPolicyTemplate implements Serializable {

  public VrrpAFailOverPolicyTemplate() {
    _gateways = ImmutableMap.of();
  }

  /** gatewy IP -> weight */
  public @Nonnull Map<Ip, Integer> getGateways() {
    return _gateways;
  }

  public void addOrReplaceGateway(Ip ip, int weight) {
    ImmutableMap.Builder<Ip, Integer> gateways = ImmutableMap.builder();
    _gateways.forEach(
        (eIp, eWeight) -> {
          if (eIp.equals(ip)) {
            return;
          }
          gateways.put(eIp, eWeight);
        });
    gateways.put(ip, weight);
    _gateways = gateways.build();
  }

  /** gatewy IP -> weight */
  private @Nonnull Map<Ip, Integer> _gateways;
}
