package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class CpmiAnyObject extends TypedManagementObject {

  @JsonCreator
  private static @Nonnull CpmiAnyObject create(
      @JsonProperty(PROP_NAME) @Nullable String name, @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new CpmiAnyObject(name, uid);
  }

  private CpmiAnyObject(String name, Uid uid) {
    super(name, uid);
  }
}
