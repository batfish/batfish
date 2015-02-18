package org.batfish.representation.juniper;

import java.io.Serializable;

import org.batfish.representation.IpAccessListLine;

public abstract class FwFrom implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public abstract void applyTo(IpAccessListLine line);

}
