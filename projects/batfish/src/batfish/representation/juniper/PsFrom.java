package batfish.representation.juniper;

import java.io.Serializable;

import batfish.representation.PolicyMapMatchLine;

public abstract class PsFrom implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public abstract PolicyMapMatchLine toPolicyMapMatchLine();

}
