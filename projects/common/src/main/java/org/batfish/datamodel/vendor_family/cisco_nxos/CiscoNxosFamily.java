package org.batfish.datamodel.vendor_family.cisco_nxos;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class CiscoNxosFamily implements Serializable {

  private static final String PROP_MAJOR_VERSION = "majorVersion";
  private static final String PROP_PLATFORM = "platform";

  public static final class Builder {

    public @Nonnull CiscoNxosFamily build() {
      return new CiscoNxosFamily(_majorVersion, _platform);
    }

    public @Nonnull Builder setMajorVersion(NxosMajorVersion majorVersion) {
      _majorVersion = majorVersion;
      return this;
    }

    public @Nonnull Builder setPlatform(NexusPlatform platform) {
      _platform = platform;
      return this;
    }

    private @Nonnull NxosMajorVersion _majorVersion;
    private @Nonnull NexusPlatform _platform;

    private Builder() {
      _majorVersion = NxosMajorVersion.UNKNOWN;
      _platform = NexusPlatform.UNKNOWN;
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @JsonProperty(PROP_MAJOR_VERSION)
  public NxosMajorVersion getMajorVersion() {
    return _majorVersion;
  }

  @JsonProperty(PROP_PLATFORM)
  public @Nonnull NexusPlatform getPlatform() {
    return _platform;
  }

  private final @Nonnull NxosMajorVersion _majorVersion;
  private final @Nonnull NexusPlatform _platform;

  @JsonCreator
  private static @Nonnull CiscoNxosFamily create(
      @JsonProperty(PROP_MAJOR_VERSION) @Nullable NxosMajorVersion majorVersion,
      @JsonProperty(PROP_PLATFORM) @Nullable NexusPlatform platform) {
    return new CiscoNxosFamily(
        firstNonNull(majorVersion, NxosMajorVersion.UNKNOWN),
        firstNonNull(platform, NexusPlatform.UNKNOWN));
  }

  private CiscoNxosFamily(NxosMajorVersion majorVersion, NexusPlatform platform) {
    _majorVersion = majorVersion;
    _platform = platform;
  }
}
