package org.batfish.dataplane.ibdp;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.dataplane.ibdp.DataplaneUtil.computeConfigurations;
import static org.batfish.dataplane.ibdp.DataplaneUtil.computeFibs;
import static org.batfish.dataplane.ibdp.DataplaneUtil.computeForwardingAnalysis;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.vxlan.Layer2Vni;

/**
 * Partial dataplane to be used during dataplane computation. Should not be used outside of the
 * engine. Does not implement full dataplane, only FIBs and Forwarding analysis, to enable
 * traceroutes within dataplane computation engine.
 */
@ParametersAreNonnullByDefault
public final class PartialDataplane implements DataPlane {

  @Override
  public Map<String, Map<String, Fib>> getFibs() {
    return _fibs;
  }

  @Override
  public ForwardingAnalysis getForwardingAnalysis() {
    return _forwardingAnalysis;
  }

  @Nonnull
  @Override
  public Table<String, String, Set<Layer2Vni>> getLayer2Vnis() {
    return _vniSettings;
  }

  @Nonnull
  @Override
  public Table<String, String, Set<Bgpv4Route>> getBgpRoutes() {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public Table<String, String, Set<EvpnRoute<?, ?>>> getEvpnRoutes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
      getPrefixTracingInfoSummary() {
    throw new UnsupportedOperationException();
  }

  @Override
  public SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> getRibs() {
    throw new UnsupportedOperationException();
  }

  //////////
  // Builder
  //////////

  public static class Builder {

    @Nullable private Map<String, Node> _nodes;
    @Nullable private Topology _layer3Topology;

    public Builder setNodes(@Nonnull Map<String, Node> nodes) {
      _nodes = ImmutableMap.copyOf(nodes);
      return this;
    }

    public Builder setLayer3Topology(@Nonnull Topology layer3Topology) {
      _layer3Topology = layer3Topology;
      return this;
    }

    public PartialDataplane build() {
      return new PartialDataplane(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  /////////////////////////
  // Private implementation
  /////////////////////////

  @Nonnull private final Map<String, Map<String, Fib>> _fibs;
  @Nonnull private final ForwardingAnalysis _forwardingAnalysis;
  @Nonnull private final Table<String, String, Set<Layer2Vni>> _vniSettings;

  private PartialDataplane(Builder builder) {
    checkArgument(builder._nodes != null, "Dataplane must have nodes to be constructed");
    checkArgument(builder._layer3Topology != null, "Dataplane must have an L3 topology set");

    Map<String, Node> nodes = builder._nodes;
    Map<String, Configuration> configs = computeConfigurations(nodes);
    _fibs = computeFibs(nodes);
    _forwardingAnalysis = computeForwardingAnalysis(_fibs, configs, builder._layer3Topology);
    _vniSettings = DataplaneUtil.computeVniSettings(nodes);
  }
}
