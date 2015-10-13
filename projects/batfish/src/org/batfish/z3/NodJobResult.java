package org.batfish.z3;

import java.util.Collections;
import java.util.Set;

import org.batfish.representation.Flow;

public class NodJobResult {

   /**
    * Elapsed time in milliseconds
    */
   private long _elapsedTime;

   private Throwable _failureCause;

   private Set<Flow> _flows;

   private boolean _successful;

   public NodJobResult(long elapsedTime) {
      _successful = true;
      _flows = Collections.<Flow> emptySet();
      _elapsedTime = elapsedTime;
   }

   public NodJobResult(long elapsedTime, Set<Flow> flows) {
      _flows = flows;
      _successful = true;
      _elapsedTime = elapsedTime;
   }

   public NodJobResult(long elapsedTime, Throwable failureCause) {
      _failureCause = failureCause;
      _successful = false;
      _elapsedTime = elapsedTime;
   }

   public long getElapsedTime() {
      return _elapsedTime;
   }

   public Throwable getFailureCause() {
      return _failureCause;
   }

   public Set<Flow> getFlows() {
      return _flows;
   }

   public boolean terminatedSuccessfully() {
      return _successful;
   }

}
