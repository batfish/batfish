package org.batfish.job;

import javax.annotation.Nullable;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.datamodel.answers.AnswerElement;

public abstract class BatfishJobResult<OutputT, AnswerElementT extends AnswerElement> {

  private final long _elapsedTime;

  protected final Throwable _failureCause;

  protected final BatfishLoggerHistory _history;

  public BatfishJobResult(long elapsedTime, BatfishLoggerHistory history) {
    this(elapsedTime, history, null);
  }

  public BatfishJobResult(
      long elapsedTime, BatfishLoggerHistory history, @Nullable Throwable failureCause) {
    _elapsedTime = elapsedTime;
    _history = history;
    _failureCause = failureCause;
  }

  public abstract void appendHistory(BatfishLogger logger);

  public abstract void applyTo(OutputT output, BatfishLogger logger, AnswerElementT answerElement);

  public final long getElapsedTime() {
    return _elapsedTime;
  }

  @Nullable
  public final Throwable getFailureCause() {
    return _failureCause;
  }

  public BatfishLoggerHistory getHistory() {
    return _history;
  }
}
