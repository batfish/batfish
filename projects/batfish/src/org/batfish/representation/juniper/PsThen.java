package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.List;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.main.Warnings;

public abstract class PsThen implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   public abstract void applyTo(List<Statement> statements,
         JuniperConfiguration juniperVendorConfiguration, Configuration c,
         Warnings warnings);

   public abstract void applyTo(PolicyMapClause clause, Configuration c,
         Warnings warnings);

}
