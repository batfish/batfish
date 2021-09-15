package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class NatSection extends NamedManagementObject implements NatRuleOrSection {

  @Override
  public <T> T accept(NatRuleOrSectionVisitor<T> visitor) {
    return visitor.visitNatSection(this);
  }

  public @Nonnull List<NatRule> getRulebase() {
    return _rulebase;
  }

  public NatSection(String name, List<NatRule> rulebase, Uid uid) {
    super(name, uid);
    _rulebase = rulebase;
  }

  @JsonCreator
  private static @Nonnull NatSection create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_RULEBASE) @Nullable List<NatRule> rulebase,
      @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(rulebase != null, "Missing %s", PROP_RULEBASE);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new NatSection(name, rulebase, uid);
  }

  @Override
  public boolean equals(Object o) {
    if (!baseEquals(o)) {
      return false;
    }
    NatSection that = (NatSection) o;
    return _rulebase.equals(that._rulebase);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _rulebase);
  }

  @Override
  public String toString() {
    return baseToStringHelper().add(PROP_RULEBASE, _rulebase).toString();
  }

  private static final String PROP_RULEBASE = "rulebase";

  private final @Nonnull List<NatRule> _rulebase;
}
