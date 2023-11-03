package org.batfish.datamodel.flow;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.FlowDiff;
import org.batfish.datamodel.flow.SetupSessionStep.SetupSessionStepDetail;

/** A {@link Step} for when a new session is created. */
@JsonTypeName("SetupSession")
public final class SetupSessionStep extends Step<SetupSessionStepDetail> {

  public static final class SetupSessionStepDetail {
    private static final String PROP_INCOMING_INTERFACES = "incomingInterfaces";
    private static final String PROP_SESSION_ACTION = "sessionAction";
    private static final String PROP_SESSION_SCOPE = "sessionScope";
    private static final String PROP_MATCH_CRITERIA = "matchCriteria";
    private static final String PROP_TRANSFORMATION = "transformation";

    private final @Nonnull SessionScope _sessionScope;
    private final @Nonnull SessionAction _sessionAction;
    private final @Nonnull SessionMatchExpr _matchCriteria;
    private final @Nonnull Set<FlowDiff> _transformation;

    private SetupSessionStepDetail(
        @Nonnull SessionScope sessionScope,
        @Nonnull SessionAction sessionAction,
        @Nonnull SessionMatchExpr matchCriteria,
        @Nonnull Set<FlowDiff> transformation) {
      _sessionScope = sessionScope;
      _sessionAction = sessionAction;
      _matchCriteria = matchCriteria;
      _transformation = ImmutableSet.copyOf(transformation);
    }

    @JsonCreator
    private static SetupSessionStepDetail jsonCreator(
        @JsonProperty(PROP_INCOMING_INTERFACES) @Nullable Set<String> incomingInterfaces,
        @JsonProperty(PROP_SESSION_ACTION) @Nullable SessionAction sessionAction,
        @JsonProperty(PROP_SESSION_SCOPE) @Nullable SessionScope sessionScope,
        @JsonProperty(PROP_MATCH_CRITERIA) @Nullable SessionMatchExpr matchCriteria,
        @JsonProperty(PROP_TRANSFORMATION) @Nullable Set<FlowDiff> transformation) {
      checkArgument(
          sessionScope != null || incomingInterfaces != null, "Missing %s", PROP_SESSION_SCOPE);
      checkArgument(sessionAction != null, "Missing %s", PROP_SESSION_ACTION);
      checkArgument(matchCriteria != null, "Missing %s", PROP_MATCH_CRITERIA);
      return new SetupSessionStepDetail(
          sessionScope != null ? sessionScope : new IncomingSessionScope(incomingInterfaces),
          sessionAction,
          matchCriteria,
          firstNonNull(transformation, ImmutableSet.of()));
    }

    /**
     * Preserved to maintain compatibility with clients. May be deleted when ready to drop support
     * for versions of clients that expect SetupSessionStepDetail to have incomingInterfaces.
     */
    @Deprecated
    @JsonProperty(PROP_INCOMING_INTERFACES)
    private @Nonnull Set<String> getIncomingInterfaces() {
      return _sessionScope instanceof IncomingSessionScope
          ? ((IncomingSessionScope) _sessionScope).getIncomingInterfaces()
          : ImmutableSet.of();
    }

    @JsonProperty(PROP_SESSION_ACTION)
    public @Nonnull SessionAction getSessionAction() {
      return _sessionAction;
    }

    @JsonProperty(PROP_SESSION_SCOPE)
    public @Nonnull SessionScope getSessionScope() {
      return _sessionScope;
    }

    @JsonProperty(PROP_MATCH_CRITERIA)
    public @Nonnull SessionMatchExpr getMatchCriteria() {
      return _matchCriteria;
    }

    @JsonProperty(PROP_TRANSFORMATION)
    public @Nonnull Set<FlowDiff> getTransformation() {
      return _transformation;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof SetupSessionStepDetail)) {
        return false;
      }
      SetupSessionStepDetail that = (SetupSessionStepDetail) o;
      return _sessionScope.equals(that._sessionScope)
          && _sessionAction.equals(that._sessionAction)
          && _matchCriteria.equals(that._matchCriteria)
          && _transformation.equals(that._transformation);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_sessionScope, _sessionAction, _matchCriteria, _transformation);
    }

    public static Builder builder() {
      return new Builder();
    }

    /** Chained builder to create a {@link SetupSessionStepDetail} object */
    public static class Builder {
      private @Nullable SessionScope _sessionScope;
      private @Nullable SessionAction _sessionAction;
      private @Nullable SessionMatchExpr _matchCriteria;
      private @Nullable Set<FlowDiff> _transformation;

      public SetupSessionStepDetail build() {
        checkNotNull(
            _sessionScope, "Cannot build SetupSessionStepDetail without specifying session scope");
        checkNotNull(
            _sessionAction,
            "Cannot build SetupSessionStepDetail without specifying session action");
        checkNotNull(
            _matchCriteria,
            "Cannot build SetupSessionStepDetail without specifying match criteria");
        return new SetupSessionStepDetail(
            _sessionScope,
            _sessionAction,
            _matchCriteria,
            firstNonNull(_transformation, ImmutableSet.of()));
      }

      public Builder setSessionScope(SessionScope sessionScope) {
        _sessionScope = sessionScope;
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

      /** Only for use by {@link SetupSessionStepDetail#builder()}. */
      private Builder() {}
    }
  }

  @JsonCreator
  private static SetupSessionStep jsonCreator(
      @JsonProperty(PROP_DETAIL) @Nullable SetupSessionStepDetail detail,
      @JsonProperty(PROP_ACTION) @Nullable StepAction action) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(detail != null, "Missing %s", PROP_DETAIL);
    return new SetupSessionStep(detail);
  }

  public SetupSessionStep(SetupSessionStepDetail detail) {
    super(detail, StepAction.SETUP_SESSION);
  }
}
