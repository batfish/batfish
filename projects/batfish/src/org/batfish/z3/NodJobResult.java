package org.batfish.z3;

public class NodJobResult {

   private Throwable _failureCause;

   private String _result;

   private boolean _successful;

   public NodJobResult(String result) {
      _result = result;
      _successful = true;
   }

   public NodJobResult(Throwable failureCause) {
      _failureCause = failureCause;
      _successful = false;
   }

   public Throwable getFailureCause() {
      return _failureCause;
   }

   public String getResult() {
      return _result;
   }

   public boolean terminatedSuccessfully() {
      return _successful;
   }

}
