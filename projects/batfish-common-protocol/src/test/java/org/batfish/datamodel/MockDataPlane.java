package org.batfish.datamodel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.datamodel.vxlan.Layer3Vni;

public class MockDataPlane implements DataPlane {

  public static class Builder {
    @Nonnull private Table<String, String, Set<Bgpv4Route>> _bgpRoutes;
    @Nonnull private Table<String, String, Set<Bgpv4Route>> _bgpBackupRoutes;
    @Nonnull private Table<String, String, Set<EvpnRoute<?, ?>>> _evpnRoutes;
    @Nonnull private Table<String, String, Set<EvpnRoute<?, ?>>> _evpnBackupRoutes;
    @Nonnull private Map<String, Map<String, Fib>> _fibs;
    @Nonnull private ForwardingAnalysis _forwardingAnalysis;
    @Nonnull private Table<String, String, Set<Layer2Vni>> _layer2VniSettings;
    @Nonnull private Table<String, String, Set<Layer3Vni>> _layer3VniSettings;
    private @Nonnull SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
        _prefixTracingInfoSummary;
    private @Nonnull SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>>
        _ribs;

    private Builder() {
      _bgpRoutes = ImmutableTable.of();
      _bgpBackupRoutes = ImmutableTable.of();
      _evpnRoutes = ImmutableTable.of();
      _evpnBackupRoutes = ImmutableTable.of();
      _fibs = ImmutableMap.of();
      _forwardingAnalysis = MockForwardingAnalysis.builder().build();
      _layer2VniSettings = ImmutableTable.of();
      _layer3VniSettings = ImmutableTable.of();
      _prefixTracingInfoSummary = ImmutableSortedMap.of();
      _ribs = ImmutableSortedMap.of();
    }

    public MockDataPlane build() {
      return new MockDataPlane(this);
    }

    public Builder setBgpRoutes(@Nonnull Table<String, String, Set<Bgpv4Route>> bgpRoutes) {
      _bgpRoutes = bgpRoutes;
      return this;
    }

    public Builder setBgpBackupRoutes(
        @Nonnull Table<String, String, Set<Bgpv4Route>> bgpBackupRoutes) {
      _bgpBackupRoutes = bgpBackupRoutes;
      return this;
    }

    public Builder setEvpnRoutes(@Nonnull Table<String, String, Set<EvpnRoute<?, ?>>> evpnRoutes) {
      _evpnRoutes = evpnRoutes;
      return this;
    }

    public Builder setEvpnBackupRoutes(
        @Nonnull Table<String, String, Set<EvpnRoute<?, ?>>> evpnBackupRoutes) {
      _evpnBackupRoutes = evpnBackupRoutes;
      return this;
    }

    public Builder setFibs(@Nonnull Map<String, Map<String, Fib>> fibs) {
      _fibs = fibs;
      return this;
    }

    public Builder setForwardingAnalysis(ForwardingAnalysis forwardingAnalysis) {
      _forwardingAnalysis = forwardingAnalysis;
      return this;
    }

    public Builder setLayer2VniSettings(
        @Nonnull Table<String, String, Set<Layer2Vni>> layer2VniSettings) {
      _layer2VniSettings = layer2VniSettings;
      return this;
    }

    public Builder setLayer3VniSettings(
        @Nonnull Table<String, String, Set<Layer3Vni>> layer3VniSettings) {
      _layer3VniSettings = layer3VniSettings;
      return this;
    }

    public Builder setPrefixTracingInfoSummary(
        @Nonnull
            SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
                prefixTracingInfoSummary) {
      _prefixTracingInfoSummary = prefixTracingInfoSummary;
      return this;
    }

    public Builder setRibs(
        SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> ribs) {
      _ribs = ribs;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  @Nonnull private final Table<String, String, Set<Bgpv4Route>> _bgpRoutes;
  @Nonnull private final Table<String, String, Set<Bgpv4Route>> _bgpBackupRoutes;
  @Nonnull private final Table<String, String, Set<EvpnRoute<?, ?>>> _evpnRoutes;
  @Nonnull private final Table<String, String, Set<EvpnRoute<?, ?>>> _evpnBackupRoutes;
  @Nonnull private final Map<String, Map<String, Fib>> _fibs;
  @Nonnull private final ForwardingAnalysis _forwardingAnalysis;

  @Nonnull private final Table<String, String, Set<Layer2Vni>> _layer2VniSettings;
  @Nonnull private final Table<String, String, Set<Layer3Vni>> _layer3VniSettings;
  private final @Nonnull SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
      _prefixTracingInfoSummary;
  private final @Nonnull SortedMap<
          String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>>
      _ribs;

  private MockDataPlane(Builder builder) {
    _bgpRoutes = builder._bgpRoutes;
    _bgpBackupRoutes = builder._bgpBackupRoutes;
    _evpnRoutes = builder._evpnRoutes;
    _evpnBackupRoutes = builder._evpnBackupRoutes;
    _fibs = builder._fibs;
    _forwardingAnalysis = builder._forwardingAnalysis;
    _layer2VniSettings = builder._layer2VniSettings;
    _layer3VniSettings = builder._layer3VniSettings;
    _prefixTracingInfoSummary = builder._prefixTracingInfoSummary;
    _ribs = ImmutableSortedMap.copyOf(builder._ribs);
  }

  @Nonnull
  @Override
  public Table<String, String, Set<Bgpv4Route>> getBgpRoutes() {
    return _bgpRoutes;
  }

  @Nonnull
  @Override
  public Table<String, String, Set<Bgpv4Route>> getBgpBackupRoutes() {
    return _bgpBackupRoutes;
  }

  @Nonnull
  @Override
  public Table<String, String, Set<EvpnRoute<?, ?>>> getEvpnRoutes() {
    return _evpnRoutes;
  }

  @Nonnull
  @Override
  public Table<String, String, Set<EvpnRoute<?, ?>>> getEvpnBackupRoutes() {
    return _evpnBackupRoutes;
  }

  @Nonnull
  @Override
  public Map<String, Map<String, Fib>> getFibs() {
    return _fibs;
  }

  @Override
  public @Nonnull ForwardingAnalysis getForwardingAnalysis() {
    return _forwardingAnalysis;
  }

  @Nonnull
  @Override
  public SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> getRibs() {
    return _ribs;
  }

  @Override
  @Nonnull
  public SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
      getPrefixTracingInfoSummary() {
    return _prefixTracingInfoSummary;
  }

  @Nonnull
  @Override
  public Table<String, String, Set<Layer2Vni>> getLayer2Vnis() {
    return _layer2VniSettings;
  }

  @Nonnull
  @Override
  public Table<String, String, Set<Layer3Vni>> getLayer3Vnis() {
    return _layer3VniSettings;
  }
}
