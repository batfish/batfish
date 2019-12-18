package org.batfish.representation.palo_alto;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;

/** A {@link PolicyRuleMatch} that matches on a set of {@link AddressPrefix}. */
public final class PolicyRuleMatchAddressPrefixSet implements PolicyRuleMatch {

  private final @Nonnull Set<AddressPrefix> _addressPrefixes;

  public PolicyRuleMatchAddressPrefixSet() {
    _addressPrefixes = new HashSet<>();
  }

  @Override
  public <T> T accept(PolicyRuleMatchVisitor<T> visitor) {
    return visitor.visitPolicyRuleMatchAddressPrefixSet(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PolicyRuleMatchAddressPrefixSet)) {
      return false;
    }
    PolicyRuleMatchAddressPrefixSet that = (PolicyRuleMatchAddressPrefixSet) o;
    return _addressPrefixes.equals(that._addressPrefixes);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_addressPrefixes);
  }

  public @Nonnull Set<AddressPrefix> getAddressPrefixes() {
    return _addressPrefixes;
  }
}
