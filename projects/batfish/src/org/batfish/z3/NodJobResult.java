package org.batfish.z3;

public class NodJobResult {

   private Throwable _failureCause;

   private boolean _successful;

   public NodJobResult(boolean successful, Throwable failureCause) {
      _failureCause = failureCause;
      _successful = successful;
   }

   public Throwable getFailureCause() {
      return _failureCause;
   }

   public boolean terminatedSuccessfully() {
      return _successful;
   }

}
