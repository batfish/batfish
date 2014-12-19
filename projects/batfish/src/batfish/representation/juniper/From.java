package batfish.representation.juniper;

import java.io.Serializable;

import batfish.representation.PolicyMapMatchLine;

public abstract class From implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public abstract PolicyMapMatchLine toPolicyMapMatchLine();

}
