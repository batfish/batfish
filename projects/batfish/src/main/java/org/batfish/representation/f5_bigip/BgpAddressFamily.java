package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Top-level BGP-process-wide address-family configuration */
@ParametersAreNonnullByDefault
public abstract class BgpAddressFamily implements Serializable {

  private static final long serialVersionUID = 1L;
}
