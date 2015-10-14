package org.batfish.job;

import org.batfish.common.BatfishLogger;

public abstract class BatfishJobResult<Output> {

   private final long _elapsedTime;

   private Throwable _failureCause;

   public BatfishJobResult(long elapsedTime) {
      _elapsedTime = elapsedTime;
   }

   public BatfishJobResult(long elapsedTime, Throwable failureCause) {
      _elapsedTime = elapsedTime;
      _failureCause = failureCause;
   }

   public abstract void applyTo(Output output, BatfishLogger logger);

   public abstract void explainFailure(BatfishLogger logger);

   public final long getElapsedTime() {
      return _elapsedTime;
   }

   public final Throwable getFailureCause() {
      return _failureCause;
   }

}
