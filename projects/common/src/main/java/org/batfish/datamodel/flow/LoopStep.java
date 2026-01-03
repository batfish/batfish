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

    @Override
    public boolean equals(Object o) {
      return o instanceof LoopStepDetail;
    }

    @Override
    public int hashCode() {
      return LoopStepDetail.class.hashCode();
    }
  }

  @SuppressWarnings("PMD.UnusedFormalParameter")
  @JsonCreator
  private static LoopStep jsonCreator(
      @JsonProperty(Step.PROP_ACTION) @Nullable String action,
      @JsonProperty(Step.PROP_DETAIL) @Nullable String detail) {
    return INSTANCE;
  }
}
