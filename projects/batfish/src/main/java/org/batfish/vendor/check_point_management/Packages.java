package org.batfish.vendor.check_point_management;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Data model for the response to the {@code show-packages} command. */
public final class Packages implements Serializable {

  @VisibleForTesting
  Packages(Map<Uid, Package> packages) {
    _packages = packages;
  }

  @JsonCreator
  private static @Nonnull Packages create(
      @JsonProperty(PROP_PACKAGES) @Nullable List<Package> packages) {
    checkArgument(packages != null, "Missing %s", PROP_PACKAGES);
    return new Packages(
        packages.stream()
            .collect(
                ImmutableMap.toImmutableMap(NamedManagementObject::getUid, Function.identity())));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Packages)) {
      return false;
    }
    Packages that = (Packages) o;
    return _packages.equals(that._packages);
  }

  @Override
  public int hashCode() {
    return _packages.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_packages", _packages).toString();
  }

  public @Nonnull Map<Uid, Package> getPackages() {
    return _packages;
  }

  private static final String PROP_PACKAGES = "packages";

  private final @Nonnull Map<Uid, Package> _packages;
}
