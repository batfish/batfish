package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.flow.PolicyStep.PolicyStepDetail;
import org.batfish.datamodel.packet_policy.PacketPolicy;

/** Step indicating a packet was processed by a {@link PacketPolicy} */
@JsonTypeName("Policy")
public class PolicyStep extends Step<PolicyStepDetail> {

  public PolicyStep(PolicyStepDetail detail, StepAction action) {
    super(detail, action);
  }

  public static final class PolicyStepDetail {
    private static final String PROP_POLICY = "policy";
    private final @Nonnull String _policy;

    public PolicyStepDetail(String policy) {
      _policy = policy;
    }

    public @Nonnull String getPolicy() {
      return _policy;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof PolicyStepDetail)) {
        return false;
      }
      PolicyStepDetail that = (PolicyStepDetail) o;
      return _policy.equals(that._policy);
    }

    @Override
    public int hashCode() {
      return _policy.hashCode();
    }

    @JsonCreator
    private static PolicyStepDetail create(@JsonProperty(PROP_POLICY) @Nullable String policy) {
      checkArgument(policy != null);
      return new PolicyStepDetail(policy);
    }
  }

  private static final String PROP_DETAIL = "detail";
  private static final String PROP_ACTION = "action";

  @JsonCreator
  private static PolicyStep jsonCreator(
      @JsonProperty(PROP_DETAIL) @Nullable PolicyStepDetail detail,
      @JsonProperty(PROP_ACTION) @Nullable StepAction action) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(detail != null, "Missing %s", PROP_DETAIL);
    return new PolicyStep(detail, action);
  }
}
