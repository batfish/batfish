package org.batfish.representation.juniper;

import java.io.Serializable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;

public abstract class FwFrom implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  public abstract void applyTo(
      HeaderSpace.Builder headerSpaceBuilder, JuniperConfiguration jc, Warnings w, Configuration c);
}
