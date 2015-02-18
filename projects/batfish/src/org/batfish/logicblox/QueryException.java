package org.batfish.logicblox;

public class QueryException extends Exception {
   private static final long serialVersionUID = 1L;

   public QueryException(String s) {
      super(s);
   }

   public QueryException(Throwable t) {
      super(t);
   }
}
