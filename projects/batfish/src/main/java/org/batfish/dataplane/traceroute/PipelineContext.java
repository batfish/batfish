package org.batfish.dataplane.traceroute;

import static com.google.common.base.Preconditions.checkState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;

public final class PipelineContext {

  @Nonnull private final Flow _flow;
  @Nullable private final ExitPoint _exitPoint;

  private PipelineContext(@Nonnull Flow flow, @Nullable ExitPoint exitPoint) {
    _flow = flow;
    _exitPoint = exitPoint;
  }

  public static Builder builder(Flow flow) {
    return new Builder().setFlow(flow);
  }

  public static PipelineContext of(Flow flow) {
    return builder(flow).build();
  }

  @Nonnull
  public Flow getFlow() {
    return _flow;
  }

  @Nullable
  public ExitPoint getExitPoint() {
    return _exitPoint;
  }

  public Builder toBuilder() {
    return builder(_flow).setExitPoint(_exitPoint);
  }

  public static final class Builder {
    private Flow _flow;
    @Nullable private ExitPoint _exitPoint;

    private Builder() {}

    public Builder setFlow(Flow flow) {
      _flow = flow;
      return this;
    }

    public Builder setExitPoint(ExitPoint exitPoint) {
      _exitPoint = exitPoint;
      return this;
    }

    public PipelineContext build() {
      checkState(_flow != null, "Cannot have context without a flow");
      return new PipelineContext(_flow, _exitPoint);
    }
  }
}
