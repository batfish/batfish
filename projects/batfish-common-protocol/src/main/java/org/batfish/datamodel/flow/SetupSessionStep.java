package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.flow.StepAction.SETUP_SESSION;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nullable;
import org.batfish.datamodel.flow.SetupSessionStep.SetupSessionStepDetail;

/** A {@link Step} for when a new session is created. */
@JsonTypeName("SetupSession")
public final class SetupSessionStep extends Step<SetupSessionStepDetail> {
  public SetupSessionStep() {
    super(DETAIL, SETUP_SESSION);
  }

  @JsonInclude // if not present, empty object will be omitted
  static final class SetupSessionStepDetail {}

  private static final SetupSessionStepDetail DETAIL = new SetupSessionStepDetail();

  @JsonCreator
  private static SetupSessionStep jsonCreator(
      @Nullable @JsonProperty(PROP_DETAIL) SetupSessionStepDetail detail,
      @Nullable @JsonProperty(PROP_ACTION) StepAction action) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(detail != null, "Missing %s", PROP_DETAIL);
    return new SetupSessionStep();
  }
}
