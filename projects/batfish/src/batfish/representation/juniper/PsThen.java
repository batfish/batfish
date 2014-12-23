package batfish.representation.juniper;

import java.io.Serializable;

import batfish.representation.PolicyMapClause;
import batfish.representation.PolicyMapSetLine;

public abstract class PsThen implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public abstract void applyTo(PolicyMapClause clause);

   public abstract PolicyMapSetLine toPolicyStatmentSetLine();

}
