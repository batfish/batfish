package org.batfish.representation.juniper;

import java.io.Serializable;

public class NoPortTranslation implements PortAddressTranslation, Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  public static final NoPortTranslation INSTANCE = new NoPortTranslation();

  private NoPortTranslation() {}
}
