package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import javax.annotation.Nonnull;

@JsonSchemaDescription("A generated/aggregate IPV4 route.")
public final class GeneratedRoute extends AbstractRoute {

  public static class Builder extends AbstractRouteBuilder<Builder, GeneratedRoute> {

    private List<SortedSet<Integer>> _asPath;

    private String _attributePolicy;

    private boolean _discard;

    private String _generationPolicy;

    private String _nextHopInterface;

    public Builder() {
      _asPath = new ArrayList<>();
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    @Override
    public GeneratedRoute build() {
      GeneratedRoute gr =
          new GeneratedRoute(
              getNetwork(),
              getAdmin(),
              getNextHopIp(),
              new AsPath(_asPath),
              _attributePolicy,
              _discard,
              _generationPolicy,
              getMetric(),
              _nextHopInterface);
      return gr;
    }

    public void setAsPath(List<SortedSet<Integer>> asPath) {
      _asPath = asPath;
    }

    public void setAttributePolicy(String attributePolicy) {
      _attributePolicy = attributePolicy;
    }

    public void setDiscard(boolean discard) {
      _discard = discard;
    }

    public void setGenerationPolicy(String generationPolicy) {
      _generationPolicy = generationPolicy;
    }

    public void setNextHopInterface(String nextHopInterface) {
      _nextHopInterface = nextHopInterface;
    }
  }

  private static final String AS_PATH_VAR = "asPath";

  private static final String ATTRIBUTE_POLICY_VAR = "attributePolicy";

  private static final String DISCARD_VAR = "discard";

  private static final String GENERATION_POLICY_VAR = "generationPolicy";

  private static final String METRIC_VAR = "metric";

  private static final long serialVersionUID = 1L;

  private final int _administrativeCost;

  private final AsPath _asPath;

  private final String _attributePolicy;

  private final boolean _discard;

  private final String _generationPolicy;

  private final Integer _metric;

  private final String _nextHopInterface;

  private final Ip _nextHopIp;

  @JsonCreator
  public GeneratedRoute(
      @JsonProperty(NETWORK_VAR) Prefix network,
      @JsonProperty(ADMINISTRATIVE_COST_VAR) int administrativeCost,
      @JsonProperty(NEXT_HOP_IP_VAR) Ip nextHopIp,
      @JsonProperty(AS_PATH_VAR) AsPath asPath,
      @JsonProperty(ATTRIBUTE_POLICY_VAR) String attributePolicy,
      @JsonProperty(DISCARD_VAR) boolean discard,
      @JsonProperty(GENERATION_POLICY_VAR) String generationPolicy,
      @JsonProperty(METRIC_VAR) Integer metric,
      @JsonProperty(NEXT_HOP_INTERFACE_VAR) String nextHopInterface) {
    super(network);
    _administrativeCost = administrativeCost;
    _asPath = asPath;
    _attributePolicy = attributePolicy;
    _discard = discard;
    _generationPolicy = generationPolicy;
    _metric = metric;
    _nextHopIp = firstNonNull(nextHopIp, Route.UNSET_ROUTE_NEXT_HOP_IP);
    _nextHopInterface = firstNonNull(nextHopInterface, Route.UNSET_NEXT_HOP_INTERFACE);
  }

  @Override
  public boolean equals(Object o) {
    GeneratedRoute rhs = (GeneratedRoute) o;
    return _network.equals(rhs._network);
  }

  @JsonIgnore(false)
  @JsonProperty(ADMINISTRATIVE_COST_VAR)
  @Override
  public int getAdministrativeCost() {
    return _administrativeCost;
  }

  @JsonProperty(AS_PATH_VAR)
  @JsonPropertyDescription("A BGP AS-path attribute to associate with this generated route")
  public AsPath getAsPath() {
    return _asPath;
  }

  @JsonProperty(ATTRIBUTE_POLICY_VAR)
  @JsonPropertyDescription("The name of the policy that sets attributes of this route")
  public String getAttributePolicy() {
    return _attributePolicy;
  }

  @JsonProperty(DISCARD_VAR)
  @JsonPropertyDescription("Whether this route is route is meant to discard all matching packets")
  public boolean getDiscard() {
    return _discard;
  }

  @JsonProperty(GENERATION_POLICY_VAR)
  @JsonPropertyDescription(
      "The name of the policy that will generate this route if another route matches it")
  public String getGenerationPolicy() {
    return _generationPolicy;
  }

  @JsonIgnore(false)
  @JsonProperty(METRIC_VAR)
  @Override
  public Integer getMetric() {
    return _metric;
  }

  @Nonnull
  @Override
  public String getNextHopInterface() {
    return _nextHopInterface;
  }

  @Nonnull
  @JsonIgnore(false)
  @JsonProperty(NEXT_HOP_IP_VAR)
  @Override
  public Ip getNextHopIp() {
    return _nextHopIp;
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.AGGREGATE;
  }

  @Override
  public int getTag() {
    return NO_TAG;
  }

  @Override
  public int hashCode() {
    return _network.hashCode();
  }

  @Override
  protected final String protocolRouteString() {
    return " asPath:"
        + _asPath
        + " attributePolicy:"
        + _attributePolicy
        + " discard:"
        + _discard
        + " generationPolicy:"
        + _generationPolicy;
  }

  @Override
  public int routeCompare(AbstractRoute rhs) {
    if (getClass() != rhs.getClass()) {
      return 0;
    }
    GeneratedRoute castRhs = (GeneratedRoute) rhs;
    int ret;
    if (_asPath == null) {
      if (castRhs._asPath != null) {
        ret = -1;
      } else {
        ret = 0;
      }
    } else if (castRhs._asPath == null) {
      ret = 1;
    } else {
      ret = _asPath.compareTo(castRhs._asPath);
    }
    if (ret != 0) {
      return ret;
    }
    if (_attributePolicy == null) {
      if (castRhs._attributePolicy != null) {
        ret = -1;
      } else {
        ret = 0;
      }
    } else if (castRhs._attributePolicy == null) {
      ret = 1;
    } else {
      ret = _attributePolicy.compareTo(castRhs._attributePolicy);
    }
    if (ret != 0) {
      return ret;
    }
    ret = Boolean.compare(_discard, castRhs._discard);
    if (ret != 0) {
      return ret;
    }
    if (_generationPolicy == null) {
      if (castRhs._generationPolicy != null) {
        ret = -1;
      } else {
        ret = 0;
      }
    } else if (castRhs._generationPolicy == null) {
      ret = 1;
    } else {
      ret = _generationPolicy.compareTo(castRhs._generationPolicy);
    }
    return ret;
  }
}
