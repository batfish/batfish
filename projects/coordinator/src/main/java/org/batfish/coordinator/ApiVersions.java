package org.batfish.coordinator;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Contains hard-coded API semversions. Each Major version num should correspond to an available
 * /v(num) endpoint. A minor version should be updated whenever a new endpoint is added.
 */
@ParametersAreNonnullByDefault
public final class ApiVersions {

  public static @Nonnull ApiVersions instance() {
    return INSTANCE;
  }

  @JsonValue
  public @Nonnull Map<Integer, String> getVersions() {
    return _versions;
  }

  private static final ApiVersions INSTANCE = new ApiVersions();

  private ApiVersions() {
    _versions = ImmutableMap.of(1, "1.0.0", 2, "2.1.0");
  }

  private final @Nonnull Map<Integer, String> _versions;

  // BELOW IS FOR TESTING
  private ApiVersions(Map<Integer, String> versions) {
    _versions = versions;
  }

  @JsonCreator
  private static @Nonnull ApiVersions create(@Nullable Map<Integer, String> versions) {
    return new ApiVersions(ImmutableMap.copyOf(firstNonNull(versions, ImmutableMap.of())));
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ApiVersions)) {
      return false;
    }
    return ((ApiVersions) obj)._versions.equals(_versions);
  }

  @Override
  public int hashCode() {
    return _versions.hashCode();
  }
}
