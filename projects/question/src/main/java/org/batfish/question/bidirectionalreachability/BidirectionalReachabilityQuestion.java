package org.batfish.question.bidirectionalreachability;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.specifiers.PathConstraintsInput;

/**
 * A question to perform a bidirectional reachability query. Searches for specified
 * forward-direction flows for which return flows successfully make it back.
 */
@ParametersAreNonnullByDefault
public final class BidirectionalReachabilityQuestion extends Question {
  // Forward flow constraints
  private static final String PROP_HEADER_CONSTRAINTS = "headers";
  private static final String PROP_PATH_CONSTRAINT = "pathConstraints";

  // Return flow constraints
  private static final String PROP_RETURN_FLOW_TYPE = "returnFlowType";

  private final @Nonnull PacketHeaderConstraints _headerConstraints;
  private final @Nonnull PathConstraintsInput _pathConstraints;
  private final @Nonnull ReturnFlowType _returnFlowType;

  @Nonnull
  BidirectionalReachabilityQuestion(
      @JsonProperty(PROP_HEADER_CONSTRAINTS) PacketHeaderConstraints headers,
      @JsonProperty(PROP_PATH_CONSTRAINT) PathConstraintsInput pathConstraints,
      @JsonProperty(PROP_RETURN_FLOW_TYPE) ReturnFlowType returnFlowType) {
    _headerConstraints = headers;
    _pathConstraints = pathConstraints;
    _returnFlowType = returnFlowType;
  }

  @JsonCreator
  private static BidirectionalReachabilityQuestion jsonCreator(
      @JsonProperty(PROP_HEADER_CONSTRAINTS) @Nullable PacketHeaderConstraints headers,
      @JsonProperty(PROP_PATH_CONSTRAINT) @Nullable PathConstraintsInput pathConstraints,
      @JsonProperty(PROP_RETURN_FLOW_TYPE) @Nullable ReturnFlowType returnFlowType) {
    return new BidirectionalReachabilityQuestion(
        firstNonNull(headers, PacketHeaderConstraints.unconstrained()),
        firstNonNull(pathConstraints, PathConstraintsInput.unconstrained()),
        firstNonNull(returnFlowType, ReturnFlowType.SUCCESS));
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  @Override
  public String getName() {
    return "bidirectionalReachability";
  }

  @JsonProperty(PROP_HEADER_CONSTRAINTS)
  public @Nonnull PacketHeaderConstraints getHeaderConstraints() {
    return _headerConstraints;
  }

  @JsonProperty(PROP_PATH_CONSTRAINT)
  public @Nonnull PathConstraintsInput getPathConstraintsInput() {
    return _pathConstraints;
  }

  @JsonProperty(PROP_RETURN_FLOW_TYPE)
  public @Nonnull ReturnFlowType getReturnFlowType() {
    return _returnFlowType;
  }
}
