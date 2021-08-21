package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A global pre-defined object. */
public abstract class Global extends TypedManagementObject {

  @Override
  public final boolean equals(Object obj) {
    return baseEquals(obj);
  }

  @Override
  public final int hashCode() {
    return baseHashcode();
  }

  @Override
  public final String toString() {
    return baseToStringHelper().toString();
  }

  protected static final String NAME_ORIGINAL = "Original";
  protected static final String NAME_POLICY_TARGETS = "Policy Targets";

  protected Global(String name, Uid uid) {
    super(name, uid);
  }

  @JsonCreator
  private static @Nonnull Global create(
      @JsonProperty(PROP_NAME) @Nullable String name, @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    switch (name) {
      case NAME_ORIGINAL:
        return new Original(uid);
      case NAME_POLICY_TARGETS:
        return new PolicyTargets(uid);
      default:
        return new UnhandledGlobal(name, uid);
    }
  }
}
