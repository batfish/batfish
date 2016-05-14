package org.batfish.representation.juniper;

import java.io.Serializable;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.main.Warnings;

public abstract class PsThen implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public abstract void applyTo(PolicyMapClause clause, Configuration c,
         Warnings warnings);

}
