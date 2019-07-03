package org.batfish.representation.juniper;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
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

  private final String _name;

  private final IpField _ipField;

  public NatRuleThenPrefixName(String name, IpField ipField) {
    _name = name;
    _ipField = ipField;
  }

  public String getName() {
    return _name;
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
    return _name.equals(that._name) && _ipField == that._ipField;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _ipField);
  }

  @Override
  public List<TransformationStep> toTransformationSteps(
      Nat nat,
      @Nullable Map<String, AddressBookEntry> addressBookEntryMap,
      Ip interfaceIp,
      Warnings warnings) {
    checkArgument(nat.getType() == Type.STATIC, "prefix name is only supported in static nat");
    checkArgument(addressBookEntryMap != null, "address book cannot be null");

    AddressBookEntry addressBookEntry = addressBookEntryMap.get(_name);

    if (!(addressBookEntry instanceof AddressAddressBookEntry)) {
      throw new BatfishException("unknown address book entry");
    }

    AddressAddressBookEntry addressAddressBookEntry = (AddressAddressBookEntry) addressBookEntry;
    Prefix subnet = addressAddressBookEntry.getIpWildcard().toPrefix();

    return ImmutableList.of(
        new ShiftIpAddressIntoSubnet(TransformationType.STATIC_NAT, _ipField, subnet));
  }
}
