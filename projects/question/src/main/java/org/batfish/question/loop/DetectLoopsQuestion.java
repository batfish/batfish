package org.batfish.question.loop;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.batfish.common.util.TracePruner.DEFAULT_MAX_TRACES;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.Question;

/** A zero-input question to check for forwarding loops. */
public class DetectLoopsQuestion extends Question {
  private static final String PROP_MAX_TRACES = "maxTraces";

  private final int _maxTraces;

  @JsonCreator
  private static DetectLoopsQuestion create(
      @Nullable @JsonProperty(PROP_MAX_TRACES) Integer maxTraces) {
    return new DetectLoopsQuestion(firstNonNull(maxTraces, DEFAULT_MAX_TRACES));
  }

  /**
   * Creates a new DetectLoops question
   *
   * @param maxTraces max number of traces displayed for flows in the answer
   */
  public DetectLoopsQuestion(int maxTraces) {
    _maxTraces = maxTraces;
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  public int getMaxTraces() {
    return _maxTraces;
  }

  @Override
  public String getName() {
    return "detectLoops";
  }
}
