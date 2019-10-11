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
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.AssignIpAddressFromPool;
import org.batfish.datamodel.transformation.AssignPortFromPool;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.PortField;
import org.batfish.datamodel.transformation.TransformationStep;

/** A {@link NatRule} that NATs using the interface's configured IP address. */
public final class NatRuleThenInterface implements NatRuleThen, Serializable {

  public static final NatRuleThenInterface INSTANCE = new NatRuleThenInterface();

  private NatRuleThenInterface() {}

  @Override
  public List<TransformationStep> toTransformationSteps(
      Nat nat,
      @Nullable Map<String, AddressBookEntry> addressBookEntryMap,
      Ip interfaceIp,
      Warnings warnings) {
    checkArgument(
        nat.getType() == SOURCE || nat.getType() == DESTINATION,
        "Interface actions can only be used in source nat and dest nat");

    TransformationType type = nat.getType().toTransformationType();

    IpField ipField = nat.getType() == SOURCE ? IpField.SOURCE : IpField.DESTINATION;

    ImmutableList.Builder<TransformationStep> builder = new Builder<>();
    builder.add(new AssignIpAddressFromPool(type, ipField, interfaceIp, interfaceIp));

    // PAT is always enabled for interface source NAT
    if (type == SOURCE_NAT) {
      builder.add(
          new AssignPortFromPool(
              type, PortField.SOURCE, nat.getDefaultFromPort(), nat.getDefaultToPort()));
    }

    return builder.build();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof NatRuleThenInterface;
  }

  @Override
  public int hashCode() {
    return NatRuleThenInterface.class.getCanonicalName().hashCode();
  }
}
