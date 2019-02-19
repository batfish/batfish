package org.batfish.representation.juniper;

import static org.batfish.datamodel.flow.TransformationStep.TransformationType.DEST_NAT;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;
import static org.batfish.representation.juniper.Nat.Type.SOURCE;
import static org.batfish.representation.juniper.Nat.Type.STATIC;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
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

  @Override
  public List<TransformationStep> toTransformationStep(Nat nat, Ip interfaceIp) {
    if (nat.getType() == STATIC) {
      throw new BatfishException("Juniper static nat is not supported");
    }

    TransformationType type = nat.getType() == SOURCE ? SOURCE_NAT : DEST_NAT;
    IpField ipField = nat.getType() == SOURCE ? IpField.SOURCE : IpField.DESTINATION;
    PortField portField = nat.getType() == SOURCE ? PortField.SOURCE : PortField.DESTINATION;

    NatPool pool = nat.getPools().get(_poolName);
    if (pool == null) {
      // pool is undefined.
      return ImmutableList.of();
    }

    ImmutableList.Builder<TransformationStep> builder = new Builder<>();
    builder.add(
        new AssignIpAddressFromPool(type, ipField, pool.getFromAddress(), pool.getToAddress()));

    PortAddressTranslation pat = pool.getPortAddressTranslation();

    if (pat != null) {
      Optional<TransformationStep> patStep = pat.toTransformationStep(type, portField);
      patStep.ifPresent(builder::add);
    } else if (type == SOURCE_NAT) {
      builder.add(
          new AssignPortFromPool(
              type, portField, nat.getDefaultFromPort(), nat.getDefaultToPort()));
    }

    return builder.build();
  }
}
