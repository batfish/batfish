package org.batfish.representation.iptables;

import java.io.Serializable;
import java.util.Map;

public class IptablesConfiguration implements Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   Map<String, Table> _tables;

   public void addRule(String table, String chain, IptablesRule rule, int i) {
      throw new UnsupportedOperationException("no implementation for generated method"); // TODO Auto-generated method stub
   }

   public void addChain(String table, String chain) {
      throw new UnsupportedOperationException("no implementation for generated method"); // TODO Auto-generated method stub
   }

   public void setPolicy(String table, String chain, String target) {
      throw new UnsupportedOperationException("no implementation for generated method"); // TODO Auto-generated method stub
   }
   
}
