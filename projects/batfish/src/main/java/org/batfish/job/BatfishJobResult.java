package org.batfish.job;

import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.datamodel.answers.AnswerElement;

public abstract class BatfishJobResult<Output, AE extends AnswerElement> {

   private final long _elapsedTime;

   protected final Throwable _failureCause;

   protected final BatfishLoggerHistory _history;

   public BatfishJobResult(long elapsedTime, BatfishLoggerHistory history) {
      _elapsedTime = elapsedTime;
      _history = history;
      _failureCause = null;
   }

   public BatfishJobResult(long elapsedTime, BatfishLoggerHistory history,
         Throwable failureCause) {
      _elapsedTime = elapsedTime;
      _history = history;
      _failureCause = failureCause;
   }

   public abstract void appendHistory(BatfishLogger logger);

   public abstract void applyTo(Output output, BatfishLogger logger,
         AE answerElement);

   public final long getElapsedTime() {
      return _elapsedTime;
   }

   public final Throwable getFailureCause() {
      return _failureCause;
   }

   public BatfishLoggerHistory getHistory() {
      return _history;
   }

}
