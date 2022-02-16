package org.batfish.representation.cisco;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An object with a readable and writable vrf. */
@ParametersAreNonnullByDefault
public interface HasWritableVrf {
  @Nullable
  String getVrf();

  void setVrf(@Nullable String vrf);
}
