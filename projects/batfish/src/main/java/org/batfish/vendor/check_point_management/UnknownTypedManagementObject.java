package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UnknownTypedManagementObject extends TypedManagementObject {

  @Override
  public boolean equals(Object o) {
    return baseEquals(o);
  }

  @Override
  public int hashCode() {
    return baseHashcode();
  }

  @JsonCreator
  private static @Nonnull UnknownTypedManagementObject create(
      @JsonProperty(PROP_NAME) @Nullable String name, @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new UnknownTypedManagementObject(name, uid);
  }

  protected UnknownTypedManagementObject(String name, Uid uid) {
    super(name, uid);
  }
}
