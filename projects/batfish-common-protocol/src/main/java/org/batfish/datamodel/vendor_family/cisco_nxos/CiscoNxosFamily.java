package org.batfish.datamodel.vendor_family.cisco_nxos;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class CiscoNxosFamily implements Serializable {

  public static final class Builder {

    public @Nonnull CiscoNxosFamily build() {
      return new CiscoNxosFamily(_platform);
    }

    public @Nonnull Builder setPlatform(NexusPlatform platform) {
      _platform = platform;
      return this;
    }

    private @Nonnull NexusPlatform _platform;

    private Builder() {
      _platform = NexusPlatform.UNKNOWN;
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public @Nonnull NexusPlatform getPlatform() {
    return _platform;
  }

  private final @Nonnull NexusPlatform _platform;

  private CiscoNxosFamily(NexusPlatform platform) {
    _platform = platform;
  }
}
