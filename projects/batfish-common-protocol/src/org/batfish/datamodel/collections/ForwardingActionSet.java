package org.batfish.datamodel.collections;

import java.util.TreeSet;

import org.batfish.datamodel.questions.ForwardingAction;

public class ForwardingActionSet extends TreeSet<ForwardingAction>{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   public ForwardingActionSet() {
      
   }
   
   public ForwardingActionSet(ForwardingAction fAction) {
      add(fAction);
   }
}
