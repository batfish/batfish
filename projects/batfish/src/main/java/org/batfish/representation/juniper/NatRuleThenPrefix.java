package org.batfish.representation.juniper;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.juniper.Nat.Type.STATIC;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.ShiftIpAddressIntoSubnet;
import org.batfish.datamodel.transformation.TransformationStep;

/** A {@link NatRule} that NATs using the configured IP Prefix. */
@ParametersAreNonnullByDefault
public class NatRuleThenPrefix implements NatRuleThen, Serializable {

  private final Prefix _prefix;

  private final IpField _ipField;

  public NatRuleThenPrefix(Prefix prefix, IpField ipField) {
    _prefix = prefix;
    _ipField = ipField;
  }

  public Prefix getPrefix() {
    return _prefix;
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
    return Objects.equals(_prefix, that._prefix) && _ipField == that._ipField;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_prefix, _ipField);
  }

  @Override
  public List<TransformationStep> toTransformationSteps(
      Nat nat,
      @Nullable Map<String, AddressBookEntry> addressBookEntryMap,
      Ip interfaceIp,
      Warnings warnings) {
    checkArgument(nat.getType() == STATIC, "Prefix can only be used in static nat");

    ImmutableList.Builder<TransformationStep> builder = new Builder<>();
    builder.add(new ShiftIpAddressIntoSubnet(TransformationType.STATIC_NAT, _ipField, _prefix));

    return builder.build();
  }
}
