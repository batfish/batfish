package org.batfish.representation.juniper;

import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.AssignIpAddressFromPool;
import org.batfish.datamodel.transformation.AssignPortFromPool;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.PortField;
import org.batfish.datamodel.transformation.TransformationStep;

/** A {@link NatRule} that nats using the specified pool */
@ParametersAreNonnullByDefault
public final class NatRuleThenPool implements NatRuleThen, Serializable {

  private static final long serialVersionUID = 1L;

  private final String _poolName;

  public NatRuleThenPool(String poolName) {
    _poolName = poolName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NatRuleThenPool)) {
      return false;
    }
    NatRuleThenPool that = (NatRuleThenPool) o;
    return Objects.equals(_poolName, that._poolName);
  }

  @Nonnull
  public String getPoolName() {
    return _poolName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_poolName);
  }

  private void applyPAT() {

  }

  @Override
  public List<TransformationStep> toTransformationStep(
      TransformationType type,
      Nat nat,
      IpField ipField,
      PortField portField,
      Map<String, NatPool> pools,
      Ip interfaceIp) {
    NatPool pool = pools.get(_poolName);
    if (pool == null) {
      // pool is undefined.
      return ImmutableList.of();
    }

    ImmutableList.Builder<TransformationStep> builder = new Builder<>();
    builder.add(
        new AssignIpAddressFromPool(type, ipField, pool.getFromAddress(), pool.getToAddress()));

    if (pool.getPatPool() != null && pool.getPatPool().getPortTranslation() || pool.getPatPool() == null && type == SOURCE_NAT) {
      applyPAT();
    }

    return builder.build();
    if (pool.getPatPool() != null && pool.getPatPool().getPortTranslation()) {
      builder.add(
                    new AssignPortFromPool(
              type, portField, nat.getDefaultFromPort(), nat.getDefaultToPort()));
      )
    }

    if (pool.getPatPool() == null) {
      if (type == SOURCE_NAT) {
        // source nat enable PAT by default
        return ImmutableList.of(
            new AssignIpAddressFromPool(type, ipField, pool.getFromAddress(), pool.getToAddress()),
            new AssignPortFromPool(
                type, portField, nat.getDefaultFromPort(), nat.getDefaultToPort()));
      }
    } else {

    }
  }
}
