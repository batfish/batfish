package org.batfish.representation.juniper;

import java.io.Serializable;

import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapClause;

public abstract class PsThen implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public abstract void applyTo(PolicyMapClause clause, Configuration c);

}
