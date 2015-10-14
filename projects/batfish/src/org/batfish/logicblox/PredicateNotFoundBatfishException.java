package org.batfish.logicblox;

import org.batfish.common.BatfishException;

public class PredicateNotFoundBatfishException extends BatfishException {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _predicate;

   public PredicateNotFoundBatfishException(String msg, String predicate) {
      super(msg);
      _predicate = predicate;
   }

   public String getPredicate() {
      return _predicate;
   }

}
