package org.batfish.datamodel.flow;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.FlowDiff;
import org.batfish.datamodel.flow.SetupSessionStep.SetupSessionStepDetail;

/** A {@link Step} for when a new session is created. */
@JsonTypeName("SetupSession")
public final class SetupSessionStep extends Step<SetupSessionStepDetail> {

  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
  public abstract static class SetupSessionStepDetail {
    static final String PROP_SESSION_ACTION = "sessionAction";
    static final String PROP_MATCH_CRITERIA = "matchCriteria";
    static final String PROP_TRANSFORMATION = "transformation";

    @Nonnull private final SessionAction _sessionAction;
    @Nonnull private final SessionMatchExpr _matchCriteria;
    @Nonnull private final Set<FlowDiff> _transformation;

    private SetupSessionStepDetail(
        @Nonnull SessionAction sessionAction,
        @Nonnull SessionMatchExpr matchCriteria,
        @Nonnull Set<FlowDiff> transformation) {
      _sessionAction = sessionAction;
      _matchCriteria = matchCriteria;
      _transformation = ImmutableSet.copyOf(transformation);
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
    @Nonnull
    public Set<FlowDiff> getTransformation() {
      return _transformation;
    }

    public abstract static class Builder<
        T extends Builder<T, U>, U extends SetupSessionStepDetail> {
      @Nullable SessionAction _sessionAction;
      @Nullable SessionMatchExpr _matchCriteria;
      @Nullable Set<FlowDiff> _transformation;

      abstract T getThis();

      public abstract U build();

      public T setSessionAction(SessionAction sessionAction) {
        _sessionAction = sessionAction;
        return getThis();
      }

      public T setMatchCriteria(SessionMatchExpr matchCriteria) {
        _matchCriteria = matchCriteria;
        return getThis();
      }

      public T setTransformation(Set<FlowDiff> transformation) {
        _transformation = transformation;
        return getThis();
      }
    }
  }

  public static final class SetupIncomingSessionStepDetail extends SetupSessionStepDetail {
    private static final String PROP_INCOMING_INTERFACES = "incomingInterfaces";

    @Nonnull private final Set<String> _incomingInterfaces;

    private SetupIncomingSessionStepDetail(
        @Nonnull Set<String> incomingInterfaces,
        @Nonnull SessionAction sessionAction,
        @Nonnull SessionMatchExpr matchCriteria,
        @Nonnull Set<FlowDiff> transformation) {
      super(sessionAction, matchCriteria, transformation);
      _incomingInterfaces = ImmutableSet.copyOf(incomingInterfaces);
    }

    @JsonCreator
    private static SetupIncomingSessionStepDetail jsonCreator(
        @Nullable @JsonProperty(PROP_INCOMING_INTERFACES) Set<String> incomingInterfaces,
        @Nullable @JsonProperty(PROP_SESSION_ACTION) SessionAction sessionAction,
        @Nullable @JsonProperty(PROP_MATCH_CRITERIA) SessionMatchExpr matchCriteria,
        @Nullable @JsonProperty(PROP_TRANSFORMATION) Set<FlowDiff> transformation) {
      checkArgument(incomingInterfaces != null, "Missing %s", PROP_INCOMING_INTERFACES);
      checkArgument(sessionAction != null, "Missing %s", PROP_SESSION_ACTION);
      checkArgument(matchCriteria != null, "Missing %s", PROP_MATCH_CRITERIA);
      return new SetupIncomingSessionStepDetail(
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

    public static Builder builder() {
      return new Builder();
    }

    public static class Builder
        extends SetupSessionStepDetail.Builder<Builder, SetupIncomingSessionStepDetail> {
      private @Nullable Set<String> _incomingInterfaces;

      @Override
      Builder getThis() {
        return this;
      }

      @Override
      public SetupIncomingSessionStepDetail build() {
        checkNotNull(
            _sessionAction,
            "Cannot build SetupIncomingSessionStepDetail without specifying session action");
        checkNotNull(
            _matchCriteria,
            "Cannot build SetupIncomingSessionStepDetail without specifying match criteria");
        return new SetupIncomingSessionStepDetail(
            firstNonNull(_incomingInterfaces, ImmutableSet.of()),
            _sessionAction,
            _matchCriteria,
            firstNonNull(_transformation, ImmutableSet.of()));
      }

      public Builder setIncomingInterfaces(Set<String> incomingInterfaces) {
        _incomingInterfaces = incomingInterfaces;
        return this;
      }

      /** Only for use by {@link SetupIncomingSessionStepDetail#builder()}. */
      private Builder() {}
    }
  }

  public static final class SetupOriginationSessionStepDetail extends SetupSessionStepDetail {
    private static final String PROP_ORIGINATING_VRF = "originatingVrf";

    @Nonnull private final String _originatingVrf;

    private SetupOriginationSessionStepDetail(
        @Nonnull String originatingVrf,
        @Nonnull SessionAction sessionAction,
        @Nonnull SessionMatchExpr matchCriteria,
        @Nonnull Set<FlowDiff> transformation) {
      super(sessionAction, matchCriteria, transformation);
      _originatingVrf = originatingVrf;
    }

    @JsonCreator
    private static SetupOriginationSessionStepDetail jsonCreator(
        @Nullable @JsonProperty(PROP_ORIGINATING_VRF) String originatingVrf,
        @Nullable @JsonProperty(PROP_SESSION_ACTION) SessionAction sessionAction,
        @Nullable @JsonProperty(PROP_MATCH_CRITERIA) SessionMatchExpr matchCriteria,
        @Nullable @JsonProperty(PROP_TRANSFORMATION) Set<FlowDiff> transformation) {
      checkArgument(originatingVrf != null, "Missing %s", PROP_ORIGINATING_VRF);
      checkArgument(sessionAction != null, "Missing %s", PROP_SESSION_ACTION);
      checkArgument(matchCriteria != null, "Missing %s", PROP_MATCH_CRITERIA);
      return new SetupOriginationSessionStepDetail(
          originatingVrf,
          sessionAction,
          matchCriteria,
          firstNonNull(transformation, ImmutableSet.of()));
    }

    @JsonProperty(PROP_ORIGINATING_VRF)
    @Nonnull
    public String getOriginatingVrf() {
      return _originatingVrf;
    }

    public static Builder builder() {
      return new Builder();
    }

    public static class Builder
        extends SetupSessionStepDetail.Builder<Builder, SetupOriginationSessionStepDetail> {
      private @Nullable String _originatingVrf;

      @Override
      Builder getThis() {
        return this;
      }

      @Override
      public SetupOriginationSessionStepDetail build() {
        checkNotNull(
            _sessionAction,
            "Cannot build SetupOriginationSessionStepDetail without specifying session action");
        checkNotNull(
            _matchCriteria,
            "Cannot build SetupOriginationSessionStepDetail without specifying match criteria");
        checkNotNull(
            _originatingVrf,
            "Cannot build MatchOriginationSessionStepDetail without specifying originating VRF");
        return new SetupOriginationSessionStepDetail(
            _originatingVrf,
            _sessionAction,
            _matchCriteria,
            firstNonNull(_transformation, ImmutableSet.of()));
      }

      public Builder setOriginatingVrf(@Nullable String originatingVrf) {
        _originatingVrf = originatingVrf;
        return this;
      }

      /** Only for use by {@link SetupOriginationSessionStepDetail#builder()}. */
      private Builder() {}
    }
  }

  @JsonCreator
  private static SetupSessionStep jsonCreator(
      @Nullable @JsonProperty(PROP_DETAIL) SetupSessionStepDetail detail,
      @Nullable @JsonProperty(PROP_ACTION) StepAction action) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(detail != null, "Missing %s", PROP_DETAIL);
    return new SetupSessionStep(detail);
  }

  public SetupSessionStep(SetupSessionStepDetail detail) {
    super(detail, StepAction.SETUP_SESSION);
  }
}
