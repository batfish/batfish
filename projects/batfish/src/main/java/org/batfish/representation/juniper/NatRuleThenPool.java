package org.batfish.representation.juniper;

import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.AssignIpAddressFromPool;
import org.batfish.datamodel.transformation.AssignPortFromPool;
import org.batfish.datamodel.transformation.IpField;
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

  @Override
  public List<TransformationStep> toTransformationStep(
      TransformationType type, IpField field, Map<String, NatPool> pools, Ip interfaceIp) {
    NatPool pool = pools.get(_poolName);
    if (pool == null) {
      // pool is undefined.
      return ImmutableList.of();
    }

    // TODO
    if (pool.getPatPool() == null && type == SOURCE_NAT) {
      // source nat enable PAT by default
      return ImmutableList.of(
          new AssignIpAddressFromPool(type, field, pool.getFromAddress(), pool.getToAddress()),
          new AssignPortFromPool(type, field, pool.getPatPool().));

    } else {
      return ImmutableList.of(
          new AssignIpAddressFromPool(type, field, pool.getFromAddress(), pool.getToAddress()));
    }
  }
}
