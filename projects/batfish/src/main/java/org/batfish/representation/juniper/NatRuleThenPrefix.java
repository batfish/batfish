package org.batfish.representation.juniper;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.juniper.Nat.Type.STATIC;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.ShiftIpAddressIntoSubnet;
import org.batfish.datamodel.transformation.TransformationStep;

/** A {@link NatRule} that NATs using the configured IP Prefix. */
@ParametersAreNonnullByDefault
public class NatRuleThenPrefix implements NatRuleThen, Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  private final Prefix _prefix;

  public NatRuleThenPrefix(Prefix prefix) {
    _prefix = prefix;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NatRuleThenPrefix)) {
      return false;
    }
    NatRuleThenPrefix that = (NatRuleThenPrefix) o;
    return Objects.equals(_prefix, that._prefix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_prefix);
  }

  @Override
  public List<TransformationStep> toTransformationSteps(
      JuniperConfiguration config, Nat nat, Ip interfaceIp, boolean reverse) {
    checkArgument(nat.getType() == STATIC, "Prefix can only be used in static nat");

    TransformationType type = nat.getType().toTransformationType();

    IpField ipField = reverse ? IpField.SOURCE : IpField.DESTINATION;

    ImmutableList.Builder<TransformationStep> builder = new Builder<>();
    builder.add(new ShiftIpAddressIntoSubnet(type, ipField, _prefix));

    return builder.build();
  }
}
