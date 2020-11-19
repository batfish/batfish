package org.batfish.datamodel.flow;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.flow.RoutingStep.RoutingStepDetail;

/**
 * {@link Step} to represent the selection of the outgoing {@link Interface} on a node for a {@link
 * org.batfish.datamodel.Flow}
 */
@JsonTypeName("Routing")
public final class RoutingStep extends Step<RoutingStepDetail> {

  /**
   * Details of {@link Step} about routing to direct a {@link Flow} from an input {@link Interface}
   * to output {@link Interface}
   */
  public static final class RoutingStepDetail {
    private static final String PROP_ROUTES = "routes";
    private static final String PROP_ARP_IP = "arpIp";
    private static final String PROP_OUTPUT_INTERFACE = "outputInterface";

    /**
     * Information about {@link Route}s which led to the selection of the out {@link Interface}, can
     * be multiple in case of ECMP
     */
    @Nonnull private final List<RouteInfo> _routes;
    /** ARP IP which was resolved using the {@code _routes} */
    @Nullable private final Ip _arpIp;
    /** Output interface which was resolved using the {@code _routes} */
    @Nullable private final String _outputInterface;

    @JsonCreator
    private RoutingStepDetail(
        @JsonProperty(PROP_ROUTES) @Nullable List<RouteInfo> routes,
        @JsonProperty(PROP_ARP_IP) @Nullable Ip arpIp,
        @JsonProperty(PROP_OUTPUT_INTERFACE) @Nullable String outputInterface) {
      _routes = firstNonNull(routes, ImmutableList.of());
      _arpIp = arpIp;
      _outputInterface = outputInterface;
    }

    @JsonProperty(PROP_ROUTES)
    @Nonnull
    public List<RouteInfo> getRoutes() {
      return _routes;
    }

    @JsonProperty(PROP_ARP_IP)
    @Nullable
    public Ip getArpIp() {
      return _arpIp;
    }

    @JsonProperty(PROP_OUTPUT_INTERFACE)
    @Nullable
    public String getOutputInterface() {
      return _outputInterface;
    }

    public static Builder builder() {
      return new Builder();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof RoutingStepDetail)) {
        return false;
      }
      RoutingStepDetail that = (RoutingStepDetail) o;
      return _routes.equals(that._routes)
          && Objects.equals(_arpIp, that._arpIp)
          && Objects.equals(_outputInterface, that._outputInterface);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_routes, _arpIp, _outputInterface);
    }

    /** Chained builder to create a {@link RoutingStepDetail} object */
    public static class Builder {
      private @Nullable List<RouteInfo> _routes;
      private @Nullable Ip _arpIp;
      private @Nullable String _outputInterface;

      public RoutingStepDetail build() {
        return new RoutingStepDetail(_routes, _arpIp, _outputInterface);
      }

      public Builder setRoutes(List<RouteInfo> routes) {
        _routes = routes;
        return this;
      }

      public Builder setArpIp(@Nullable Ip arpIp) {
        _arpIp = arpIp;
        return this;
      }

      public Builder setOutputInterface(@Nullable String outputInterface) {
        _outputInterface = outputInterface;
        return this;
      }

      /** Only for use by {@link RoutingStepDetail#builder()}. */
      private Builder() {}
    }
  }

  @JsonCreator
  private static RoutingStep jsonCreator(
      @Nullable @JsonProperty(PROP_DETAIL) RoutingStepDetail detail,
      @Nullable @JsonProperty(PROP_ACTION) StepAction action) {
    checkArgument(action != null, "Missing %s", PROP_ACTION);
    checkArgument(detail != null, "Missing %s", PROP_DETAIL);
    return new RoutingStep(detail, action);
  }

  private RoutingStep(RoutingStepDetail detail, StepAction action) {
    super(detail, action);
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Chained builder to create a {@link RoutingStep} object */
  public static final class Builder {
    private @Nullable RoutingStepDetail _detail;
    private @Nullable StepAction _action;

    public RoutingStep build() {
      checkState(_action != null, "Must call setAction before building");
      checkState(_detail != null, "Must call setDetail before building");
      return new RoutingStep(_detail, _action);
    }

    public Builder setAction(StepAction action) {
      _action = action;
      return this;
    }

    public Builder setDetail(RoutingStepDetail detail) {
      _detail = detail;
      return this;
    }

    /** Only for use by {@link RoutingStep#builder()}. */
    private Builder() {}
  }
}
