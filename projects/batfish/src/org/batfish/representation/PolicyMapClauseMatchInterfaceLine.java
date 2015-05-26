package org.batfish.representation;

public class PolicyMapClauseMatchInterfaceLine extends PolicyMapMatchLine {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _name;

   public PolicyMapClauseMatchInterfaceLine(String name) {
      _name = name;
   }

   public String getName() {
      return _name;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.INTERFACE;
   }

}
