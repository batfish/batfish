package org.batfish.datamodel.flow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nullable;
import org.batfish.datamodel.flow.LoopStep.LoopStepDetail;

/** A terminal trace {@link Step} indicating that a forwarding loop has been detected. */
@JsonTypeName("Loop")
public final class LoopStep extends Step<LoopStepDetail> {
  public static final LoopStep INSTANCE = new LoopStep();

  private LoopStep() {
    super(LOOP_STEP_DETAIL, StepAction.LOOP);
  }

  private static final LoopStepDetail LOOP_STEP_DETAIL = new LoopStepDetail();

  static final class LoopStepDetail {
    private LoopStepDetail() {}
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  @JsonCreator
  private static LoopStep jsonCreator(
      @Nullable @JsonProperty(Step.PROP_ACTION) String unusedAction,
      @Nullable @JsonProperty(Step.PROP_DETAIL) String unusedDetail) {
    return INSTANCE;
  }
}
