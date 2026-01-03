package org.batfish.datamodel.flow;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.FlowDiff;
import org.batfish.datamodel.flow.TransformationStep.TransformationStepDetail;

/** A {@link Step} for packet transformations. */
public final class TransformationStep extends Step<TransformationStepDetail> {
  /**
   * The type of transformation (i.e. what vendor-specific configuration construct) this step is
   * (partially) encoding.
   */
  public enum TransformationType {
    /** Destination nat */
    DEST_NAT,
    /** Source nat */
    SOURCE_NAT,
    /** Static nat */
    STATIC_NAT
  }

  /** Step detail for {@link TransformationStep} */
  public static final class TransformationStepDetail {
    private static final String PROP_TRANSFORMATION_TYPE = "transformationType";
    private static final String PROP_FLOW_DIFFS = "flowDiffs";

    private final @Nonnull TransformationType _type;
    private final @Nonnull SortedSet<FlowDiff> _flowDiffs;

    public TransformationStepDetail(TransformationType type, SortedSet<FlowDiff> flowDiffs) {
      _type = type;
      _flowDiffs = ImmutableSortedSet.copyOf(flowDiffs);
    }

    @JsonCreator
    private static TransformationStepDetail jsonCreator(
        @JsonProperty(PROP_TRANSFORMATION_TYPE) @Nullable TransformationType type,
        @JsonProperty(PROP_FLOW_DIFFS) @Nullable SortedSet<FlowDiff> flowDiffs) {
      checkArgument(type != null, "Missing %s", PROP_TRANSFORMATION_TYPE);
      return new TransformationStepDetail(type, firstNonNull(flowDiffs, ImmutableSortedSet.of()));
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof TransformationStepDetail)) {
        return false;
      }
      TransformationStepDetail detail = (TransformationStepDetail) o;
      return _type == detail._type && _flowDiffs.equals(detail._flowDiffs);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_type, _flowDiffs);
    }

    @JsonProperty(PROP_TRANSFORMATION_TYPE)
    public @Nonnull TransformationType getTransformationType() {
      return _type;
    }

    @JsonProperty(PROP_FLOW_DIFFS)
    public @Nonnull SortedSet<FlowDiff> getFlowDiffs() {
      return _flowDiffs;
    }
  }

  public TransformationStep(TransformationStepDetail detail, StepAction action) {
    super(detail, action);
    checkArgument(action == StepAction.TRANSFORMED || action == StepAction.PERMITTED);
    if (action == StepAction.TRANSFORMED) {
      checkArgument(
          !detail._flowDiffs.isEmpty(),
          "Cannot construct a TRANSFORMED TransformationStep with no FlowDiffs");
    } else {
      // PERMITTED transformations means it goes through the NAT without being transformed
      checkArgument(
          detail._flowDiffs.isEmpty(),
          "Cannot construct a PERMITTED TransformationStep with FlowDiffs");
    }
  }

  @JsonCreator
  private static TransformationStep jsonCreator(
      @JsonProperty(PROP_DETAIL) @Nullable TransformationStepDetail detail,
      @JsonProperty(PROP_ACTION) @Nullable StepAction action) {
    checkArgument(detail != null, "Missing %s", PROP_DETAIL);
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    return new TransformationStep(detail, action);
  }
}
