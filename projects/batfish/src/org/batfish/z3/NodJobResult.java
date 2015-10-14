package org.batfish.z3;

import java.util.Collections;
import java.util.Set;

import org.batfish.job.BatfishJobResult;
import org.batfish.common.BatfishLogger;
import org.batfish.representation.Flow;

public class NodJobResult extends BatfishJobResult<Set<Flow>> {

   /**
    * Elapsed time in milliseconds
    */
   private Set<Flow> _flows;

   public NodJobResult(long elapsedTime) {
      super(elapsedTime);
      _flows = Collections.<Flow> emptySet();
   }

   public NodJobResult(long elapsedTime, Set<Flow> flows) {
      super(elapsedTime);
      _flows = flows;
   }

   public NodJobResult(long elapsedTime, Throwable failureCause) {
      super(elapsedTime, failureCause);
   }

   @Override
   public void applyTo(Set<Flow> flows, BatfishLogger logger) {
      flows.addAll(_flows);
   }

   @Override
   public void explainFailure(BatfishLogger logger) {
   }

   public Set<Flow> getFlows() {
      return _flows;
   }

}
