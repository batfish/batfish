package org.batfish.z3;

import java.util.Map;

import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.datamodel.answers.NodFirstUnsatAnswerElement;
import org.batfish.job.BatfishJobResult;

public class NodFirstUnsatResult<Key, Result>
      extends BatfishJobResult<Map<Key, Result>, NodFirstUnsatAnswerElement> {

   private final Integer _firstUnsatQueryIndex;

   private final Key _key;

   private final Result _result;

   public NodFirstUnsatResult(Key key, Integer firstUnsatQueryIndex,
         Result result, BatfishLoggerHistory history, long elapsedTime) {
      super(elapsedTime, history);
      _firstUnsatQueryIndex = firstUnsatQueryIndex;
      _key = key;
      _result = result;
   }

   public NodFirstUnsatResult(long elapsedTime, BatfishLoggerHistory history,
         Throwable failureCause) {
      super(elapsedTime, history, failureCause);
      _key = null;
      _result = null;
      _firstUnsatQueryIndex = null;
   }

   @Override
   public void appendHistory(BatfishLogger logger) {
      logger.append(_history);
   }

   @Override
   public void applyTo(Map<Key, Result> output, BatfishLogger logger,
         NodFirstUnsatAnswerElement answerElement) {
      output.put(_key, _result);
   }

   @Override
   public String toString() {
      if (_key == null) {
         return "<FAILED>";
      }
      else if (_firstUnsatQueryIndex != null) {
         return "<FIRST_UNSAT: " + _firstUnsatQueryIndex + ":" + _result + ">";
      }
      else {
         return "<ALL_SAT>";
      }
   }

}
