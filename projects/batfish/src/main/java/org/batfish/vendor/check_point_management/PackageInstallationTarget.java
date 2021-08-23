package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Data model for an installation target under a {@link Package}. */
public final class PackageInstallationTarget extends NamedManagementObject {

  @VisibleForTesting
  PackageInstallationTarget(String name, Uid uid) {
    super(name, uid);
  }

  @JsonCreator
  private static @Nonnull PackageInstallationTarget create(
      @JsonProperty(PROP_NAME) @Nullable String name, @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new PackageInstallationTarget(name, uid);
  }

  @Override
  public boolean equals(@Nullable Object o) {
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
}
