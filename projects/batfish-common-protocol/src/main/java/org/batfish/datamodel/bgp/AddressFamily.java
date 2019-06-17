package org.batfish.datamodel.bgp;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class AddressFamily implements Serializable {
  private static final long serialVersionUID = 1L;

  @Override
  public abstract int hashCode();

  @Override
  public abstract boolean equals(@Nullable Object obj);
}
