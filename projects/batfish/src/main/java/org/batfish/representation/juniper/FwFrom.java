package org.batfish.representation.juniper;

import java.io.Serializable;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.common.Warnings;

public abstract class FwFrom implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public abstract void applyTo(IpAccessListLine line, JuniperConfiguration jc,
         Warnings w, Configuration c);

}
