package org.batfish.representation.juniper;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.ShiftIpAddressIntoSubnet;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.representation.juniper.Nat.Type;

/** A {@link NatRule} that NATs using the configured IP prefix name. */
@ParametersAreNonnullByDefault
public class NatRuleThenPrefixName implements NatRuleThen, Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  private final String _name;

  public NatRuleThenPrefixName(String name) {
    _name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NatRuleThenPrefixName)) {
      return false;
    }
    NatRuleThenPrefixName that = (NatRuleThenPrefixName) o;
    return _name.equals(that._name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name);
  }

  @Override
  public List<TransformationStep> toTransformationSteps(
      JuniperConfiguration config, Nat nat, Ip interfaceIp, boolean reverse) {
    checkArgument(nat.getType() == Type.STATIC, "prefix name is only supported in static nat");

    AddressBookEntry addressBookEntry =
        config
            .getMasterLogicalSystem()
            .getAddressBooks()
            .get(LogicalSystem.GLOBAL_ADDRESS_BOOK_NAME)
            .getEntries()
            .get(_name);

    if (!(addressBookEntry instanceof AddressAddressBookEntry)) {
      throw new BatfishException("unknown address book entry");
    }

    AddressAddressBookEntry addressAddressBookEntry = (AddressAddressBookEntry) addressBookEntry;
    Prefix subnet = addressAddressBookEntry.getIpWildcard().toPrefix();

    IpField ipField = reverse ? IpField.SOURCE : IpField.DESTINATION;
    TransformationType type = nat.getType().toTransformationType();

    return ImmutableList.of(new ShiftIpAddressIntoSubnet(type, ipField, subnet));
  }
}
