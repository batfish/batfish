package org.batfish.representation.juniper;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;
import static org.batfish.representation.juniper.Nat.Type.DESTINATION;
import static org.batfish.representation.juniper.Nat.Type.SOURCE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
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
    return Objects.hashCode(_poolName);
  }

  @Override
  public List<TransformationStep> toTransformationSteps(
      Nat nat,
      @Nullable Map<String, AddressBookEntry> addressBookEntryMap,
      Ip interfaceIp,
      Warnings warnings) {
    checkArgument(
        (nat.getType() == SOURCE || nat.getType() == DESTINATION),
        "Interface actions can only be used in source nat and dest nat");

    TransformationType type = nat.getType().toTransformationType();
    IpField ipField = nat.getType() == SOURCE ? IpField.SOURCE : IpField.DESTINATION;

    NatPool pool = nat.getPools().get(_poolName);
    if (pool == null) {
      // pool is undefined.
      return ImmutableList.of();
    }

    Ip from = pool.getFromAddress();
    Ip to = pool.getToAddress();
    if (from.asLong() > to.asLong()) {
      warnings.redFlag(String.format("NAT pool %s is invalid: %s - %s", _poolName, from, to));
      return ImmutableList.of();
    }

    ImmutableList.Builder<TransformationStep> builder = new Builder<>();
    builder.add(new AssignIpAddressFromPool(type, ipField, from, to));

    PortAddressTranslation pat = pool.getPortAddressTranslation();

    if (pat != null) {
      PortField portField = nat.getType() == SOURCE ? PortField.SOURCE : PortField.DESTINATION;
      Optional<TransformationStep> patStep = pat.toTransformationStep(type, portField);
      patStep.ifPresent(builder::add);
    } else if (type == SOURCE_NAT) {
      builder.add(
          new AssignPortFromPool(
              type, PortField.SOURCE, nat.getDefaultFromPort(), nat.getDefaultToPort()));
    }

    return builder.build();
  }
}
