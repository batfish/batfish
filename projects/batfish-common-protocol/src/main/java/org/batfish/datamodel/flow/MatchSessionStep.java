package org.batfish.datamodel.flow;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDiff;
import org.batfish.datamodel.flow.MatchSessionStep.MatchSessionStepDetail;

/** A {@link Step} for when a {@link Flow} matches a session. */
@JsonTypeName("MatchSession")
public class MatchSessionStep extends Step<MatchSessionStepDetail> {

  public static final class MatchSessionStepDetail {
    private static final String PROP_INCOMING_INTERFACES = "incomingInterfaces";
    private static final String PROP_SESSION_ACTION = "sessionAction";
    private static final String PROP_MATCH_CRITERIA = "matchCriteria";
    private static final String PROP_TRANSFORMATION = "transformation";

    @Nonnull private final Set<String> _incomingInterfaces;
    @Nonnull private final SessionAction _sessionAction;
    @Nonnull private final SessionMatchExpr _matchCriteria;
    private final Set<FlowDiff> _transformation;

    private MatchSessionStepDetail(
        @Nonnull Set<String> incomingInterfaces,
        @Nonnull SessionAction sessionAction,
        @Nonnull SessionMatchExpr matchCriteria,
        @Nonnull Set<FlowDiff> transformation) {
      _incomingInterfaces = ImmutableSet.copyOf(incomingInterfaces);
      _sessionAction = sessionAction;
      _matchCriteria = matchCriteria;
      _transformation = ImmutableSet.copyOf(transformation);
    }

    @JsonCreator
    private static MatchSessionStepDetail jsonCreator(
        @JsonProperty(PROP_INCOMING_INTERFACES) Set<String> incomingInterfaces,
        @JsonProperty(PROP_SESSION_ACTION) SessionAction sessionAction,
        @JsonProperty(PROP_MATCH_CRITERIA) SessionMatchExpr matchCriteria,
        @JsonProperty(PROP_TRANSFORMATION) Set<FlowDiff> transformation) {
      checkArgument(incomingInterfaces != null, "Missing %s", PROP_INCOMING_INTERFACES);
      checkArgument(sessionAction != null, "Missing %s", PROP_SESSION_ACTION);
      checkArgument(matchCriteria != null, "Missing %s", PROP_MATCH_CRITERIA);
      return new MatchSessionStepDetail(
          incomingInterfaces,
          sessionAction,
          matchCriteria,
          firstNonNull(transformation, ImmutableSet.of()));
    }

    @JsonProperty(PROP_INCOMING_INTERFACES)
    @Nonnull
    public Set<String> getIncomingInterfaces() {
      return _incomingInterfaces;
    }

    @JsonProperty(PROP_SESSION_ACTION)
    @Nonnull
    public SessionAction getSessionAction() {
      return _sessionAction;
    }

    @JsonProperty(PROP_MATCH_CRITERIA)
    @Nonnull
    public SessionMatchExpr getMatchCriteria() {
      return _matchCriteria;
    }

    @JsonProperty(PROP_TRANSFORMATION)
    public Set<FlowDiff> getTransformation() {
      return _transformation;
    }

    public static Builder builder() {
      return new Builder();
    }

    /** Chained builder to create a {@link MatchSessionStepDetail} object */
    public static class Builder {
      private @Nullable Set<String> _incomingInterfaces;
      private @Nullable SessionAction _sessionAction;
      private @Nullable SessionMatchExpr _matchCriteria;
      private @Nullable Set<FlowDiff> _transformation;

      public MatchSessionStepDetail build() {
        checkNotNull(
            _sessionAction,
            "Cannot build MatchSessionStepDetail without specifying session action");
        checkNotNull(
            _matchCriteria,
            "Cannot build MatchSessionStepDetail without specifying match criteria");
        return new MatchSessionStepDetail(
            firstNonNull(_incomingInterfaces, ImmutableSet.of()),
            _sessionAction,
            _matchCriteria,
            firstNonNull(_transformation, ImmutableSet.of()));
      }

      public Builder setIncomingInterfaces(Set<String> incomingInterfaces) {
        _incomingInterfaces = incomingInterfaces;
        return this;
      }

      public Builder setSessionAction(SessionAction sessionAction) {
        _sessionAction = sessionAction;
        return this;
      }

      public Builder setMatchCriteria(SessionMatchExpr matchCriteria) {
        _matchCriteria = matchCriteria;
        return this;
      }

      public Builder setTransformation(Set<FlowDiff> transformation) {
        _transformation = transformation;
        return this;
      }

      /** Only for use by {@link MatchSessionStepDetail#builder()}. */
      private Builder() {}
    }
  }

  @JsonCreator
  private static MatchSessionStep jsonCreator(
      @Nullable @JsonProperty(PROP_DETAIL) MatchSessionStepDetail detail,
      @Nullable @JsonProperty(PROP_ACTION) StepAction action) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(detail != null, "Missing %s", PROP_DETAIL);
    return new MatchSessionStep(detail);
  }

  public MatchSessionStep(MatchSessionStepDetail detail) {
    super(detail, StepAction.MATCHED_SESSION);
  }
}
