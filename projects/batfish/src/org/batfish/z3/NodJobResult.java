package org.batfish.z3;

import java.util.HashSet;
import java.util.Set;

public class NodJobResult {

   private Throwable _failureCause;

   private Set<String> _flowLines;

   private boolean _successful;

   public NodJobResult() {
      _successful = true;
      _flowLines = new HashSet<String>();
   }

   public NodJobResult(Set<String> flowLines) {
      _flowLines = flowLines;
      _successful = true;
   }

   public NodJobResult(Throwable failureCause) {
      _failureCause = failureCause;
      _successful = false;
   }

   public Throwable getFailureCause() {
      return _failureCause;
   }

   public Set<String> getFlowLines() {
      return _flowLines;
   }

   public boolean terminatedSuccessfully() {
      return _successful;
   }

}
