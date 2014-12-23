package batfish.representation.juniper;

import java.io.Serializable;

import batfish.representation.PolicyMapSetLine;

public abstract class PsThen implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public abstract PolicyMapSetLine toPolicyStatmentSetLine();

}
