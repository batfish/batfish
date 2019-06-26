package org.batfish.datamodel.vendor_family.cisco_nxos;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class CiscoNxosFamily implements Serializable {

  public static final class Builder {
    private Builder() {}

    public @Nonnull CiscoNxosFamily build() {
      return new CiscoNxosFamily();
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private CiscoNxosFamily() {}
}
