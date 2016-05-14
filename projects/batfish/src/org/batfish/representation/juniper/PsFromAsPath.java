package org.batfish.representation.juniper;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.main.Warnings;

public class PsFromAsPath extends PsFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _asPathName;

   public PsFromAsPath(String asPathName) {
      _asPathName = asPathName;
   }

   @Override
   public void applyTo(PolicyMapClause clause, PolicyStatement ps,
         JuniperConfiguration jc, Configuration c, Warnings warnings) {
      // throw new
      // UnsupportedOperationException("no implementation for generated method");
      // TODO Auto-generated method stub
   }

   public String getAsPathName() {
      return _asPathName;
   }

}
