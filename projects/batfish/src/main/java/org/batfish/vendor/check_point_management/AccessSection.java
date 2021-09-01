package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A logical grouping of access-rules in an {@link AccessLayer}. */
public final class AccessSection extends NamedManagementObject implements AccessRuleOrSection {

  public @Nonnull List<AccessRule> getRulebase() {
    return _rulebase;
  }

  @VisibleForTesting
  public AccessSection(String name, List<AccessRule> rulebase, Uid uid) {
    super(name, uid);
    _rulebase = rulebase;
  }

  @JsonCreator
  private static @Nonnull AccessSection create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_RULEBASE) @Nullable List<AccessRule> rulebase,
      @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(rulebase != null, "Missing %s", PROP_RULEBASE);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new AccessSection(name, rulebase, uid);
  }

  @Override
  public boolean equals(Object o) {
    if (!baseEquals(o)) {
      return false;
    }
    AccessSection that = (AccessSection) o;
    return _rulebase.equals(that._rulebase);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _rulebase);
  }

  private static final String PROP_RULEBASE = "rulebase";

  private final @Nonnull List<AccessRule> _rulebase;
}
