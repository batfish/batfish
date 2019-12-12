package org.batfish.datamodel.flow;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.flow.MatchSessionStep.MatchSessionStepDetail;

/** A {@link Step} for when a {@link Flow} matches a session. */
@JsonTypeName("MatchSession")
public class MatchSessionStep extends Step<MatchSessionStepDetail> {

  public static final class MatchSessionStepDetail {
    private static final String PROP_INCOMING_INTERFACES = "incomingInterfaces";

    @Nonnull private final Set<String> _incomingInterfaces;

    @JsonCreator
    private MatchSessionStepDetail(
        @JsonProperty(PROP_INCOMING_INTERFACES) @Nonnull Set<String> incomingInterfaces) {
      _incomingInterfaces = incomingInterfaces;
    }

    @JsonProperty(PROP_INCOMING_INTERFACES)
    @Nonnull
    public Set<String> getIncomingInterfaces() {
      return _incomingInterfaces;
    }

    public static Builder builder() {
      return new Builder();
    }

    /** Chained builder to create a {@link MatchSessionStepDetail} object */
    public static class Builder {
      private @Nullable Set<String> _incomingInterfaces;

      public MatchSessionStepDetail build() {
        return new MatchSessionStepDetail(_incomingInterfaces);
      }

      public Builder setIncomingInterfaces(Set<String> incomingInterfaces) {
        _incomingInterfaces = incomingInterfaces;
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

  private MatchSessionStep(MatchSessionStepDetail detail) {
    super(detail, StepAction.MATCHED_SESSION);
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Chained builder to create a {@link MatchSessionStep} object */
  public static final class Builder {
    private @Nullable MatchSessionStepDetail _detail;

    public MatchSessionStep build() {
      checkState(_detail != null, "Must call setDetail before building");
      return new MatchSessionStep(_detail);
    }

    public Builder setDetail(MatchSessionStepDetail detail) {
      _detail = detail;
      return this;
    }

    /** Only for use by {@link MatchSessionStep#builder()}. */
    private Builder() {}
  }
}
