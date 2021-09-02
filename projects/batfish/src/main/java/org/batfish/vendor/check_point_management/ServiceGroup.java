package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ServiceGroup extends TypedManagementObject implements Service {
  @Override
  public <T> T accept(ServiceVisitor<T> visitor) {
    return visitor.visitServiceGroup(this);
  }

  @Override
  public boolean equals(Object o) {
    return baseEquals(o);
  }

  @Override
  public int hashCode() {
    return baseHashcode();
  }

  @Override
  public String toString() {
    return baseToStringHelper().toString();
  }

  @JsonCreator
  private static @Nonnull ServiceGroup create(
      @JsonProperty(PROP_NAME) @Nullable String name, @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new ServiceGroup(name, uid);
  }

  @VisibleForTesting
  ServiceGroup(String name, Uid uid) {
    super(name, uid);
  }
}
