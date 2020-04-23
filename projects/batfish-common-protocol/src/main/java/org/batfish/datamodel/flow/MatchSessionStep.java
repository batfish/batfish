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
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDiff;
import org.batfish.datamodel.flow.MatchSessionStep.MatchSessionStepDetail;

/** A {@link Step} for when a {@link Flow} matches a session. */
@JsonTypeName("MatchSession")
public class MatchSessionStep extends Step<MatchSessionStepDetail> {

  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
  public abstract static class MatchSessionStepDetail {
    static final String PROP_SESSION_ACTION = "sessionAction";
    static final String PROP_MATCH_CRITERIA = "matchCriteria";
    static final String PROP_TRANSFORMATION = "transformation";

    @Nonnull private final SessionAction _sessionAction;
    @Nonnull private final SessionMatchExpr _matchCriteria;
    private final Set<FlowDiff> _transformation;

    private MatchSessionStepDetail(
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
    public Set<FlowDiff> getTransformation() {
      return _transformation;
    }

    public abstract static class Builder<
        T extends Builder<T, U>, U extends MatchSessionStepDetail> {
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

  public static final class MatchOriginationSessionStepDetail extends MatchSessionStepDetail {
    static final String PROP_ORIGINATING_VRF = "originatingVrf";
    @Nonnull private final String _originatingVrf;

    private MatchOriginationSessionStepDetail(
        @Nonnull String originatingVrf,
        @Nonnull SessionAction sessionAction,
        @Nonnull SessionMatchExpr matchCriteria,
        @Nonnull Set<FlowDiff> transformation) {
      super(sessionAction, matchCriteria, transformation);
      _originatingVrf = originatingVrf;
    }

    @JsonCreator
    private static MatchOriginationSessionStepDetail jsonCreator(
        @Nullable @JsonProperty(PROP_ORIGINATING_VRF) String originatingVrf,
        @Nullable @JsonProperty(PROP_SESSION_ACTION) SessionAction sessionAction,
        @Nullable @JsonProperty(PROP_MATCH_CRITERIA) SessionMatchExpr matchCriteria,
        @Nullable @JsonProperty(PROP_TRANSFORMATION) Set<FlowDiff> transformation) {
      checkArgument(originatingVrf != null, "Missing %s", PROP_ORIGINATING_VRF);
      checkArgument(sessionAction != null, "Missing %s", PROP_SESSION_ACTION);
      checkArgument(matchCriteria != null, "Missing %s", PROP_MATCH_CRITERIA);
      return new MatchOriginationSessionStepDetail(
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
        extends MatchSessionStepDetail.Builder<Builder, MatchOriginationSessionStepDetail> {
      private @Nullable String _originatingVrf;

      @Override
      Builder getThis() {
        return this;
      }

      @Override
      public MatchOriginationSessionStepDetail build() {
        checkNotNull(
            _sessionAction,
            "Cannot build MatchOriginationSessionStepDetail without specifying session action");
        checkNotNull(
            _matchCriteria,
            "Cannot build MatchOriginationSessionStepDetail without specifying match criteria");
        checkNotNull(
            _originatingVrf,
            "Cannot build MatchOriginationSessionStepDetail without specifying originating VRF");
        return new MatchOriginationSessionStepDetail(
            _originatingVrf,
            _sessionAction,
            _matchCriteria,
            firstNonNull(_transformation, ImmutableSet.of()));
      }

      public Builder setOriginatingVrf(String originatingVrf) {
        _originatingVrf = originatingVrf;
        return this;
      }

      /** Only for use by {@link MatchOriginationSessionStepDetail#builder()}. */
      private Builder() {}
    }
  }

  public static final class MatchIncomingSessionStepDetail extends MatchSessionStepDetail {
    static final String PROP_INCOMING_INTERFACES = "incomingInterfaces";
    @Nonnull private final Set<String> _incomingInterfaces;

    private MatchIncomingSessionStepDetail(
        @Nonnull Set<String> incomingInterfaces,
        @Nonnull SessionAction sessionAction,
        @Nonnull SessionMatchExpr matchCriteria,
        @Nonnull Set<FlowDiff> transformation) {
      super(sessionAction, matchCriteria, transformation);
      _incomingInterfaces = ImmutableSet.copyOf(incomingInterfaces);
    }

    @JsonCreator
    private static MatchIncomingSessionStepDetail jsonCreator(
        @Nullable @JsonProperty(PROP_INCOMING_INTERFACES) Set<String> incomingInterfaces,
        @Nullable @JsonProperty(PROP_SESSION_ACTION) SessionAction sessionAction,
        @Nullable @JsonProperty(PROP_MATCH_CRITERIA) SessionMatchExpr matchCriteria,
        @Nullable @JsonProperty(PROP_TRANSFORMATION) Set<FlowDiff> transformation) {
      checkArgument(incomingInterfaces != null, "Missing %s", PROP_INCOMING_INTERFACES);
      checkArgument(sessionAction != null, "Missing %s", PROP_SESSION_ACTION);
      checkArgument(matchCriteria != null, "Missing %s", PROP_MATCH_CRITERIA);
      return new MatchIncomingSessionStepDetail(
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
        extends MatchSessionStepDetail.Builder<Builder, MatchIncomingSessionStepDetail> {
      private @Nullable Set<String> _incomingInterfaces;

      @Override
      Builder getThis() {
        return this;
      }

      @Override
      public MatchIncomingSessionStepDetail build() {
        checkNotNull(
            _sessionAction,
            "Cannot build MatchIncomingSessionStepDetail without specifying session action");
        checkNotNull(
            _matchCriteria,
            "Cannot build MatchIncomingSessionStepDetail without specifying match criteria");
        return new MatchIncomingSessionStepDetail(
            firstNonNull(_incomingInterfaces, ImmutableSet.of()),
            _sessionAction,
            _matchCriteria,
            firstNonNull(_transformation, ImmutableSet.of()));
      }

      public Builder setIncomingInterfaces(Set<String> incomingInterfaces) {
        _incomingInterfaces = incomingInterfaces;
        return this;
      }

      /** Only for use by {@link MatchIncomingSessionStepDetail#builder()}. */
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
