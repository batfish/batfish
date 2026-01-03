package org.batfish.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FinalMainRib;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.datamodel.vxlan.Layer3Vni;

final class SimpleFieldsDataPlane implements DataPlane {
  private final @Nonnull Table<String, String, Set<Bgpv4Route>> _bgpRoutes;
  private final @Nonnull Table<String, String, Set<Bgpv4Route>> _bgpBackupRoutes;
  private final @Nonnull Table<String, String, Set<EvpnRoute<?, ?>>> _evpnRoutes;
  private final @Nonnull Table<String, String, Set<EvpnRoute<?, ?>>> _evpnBackupRoutes;
  private final @Nonnull Map<String, Map<String, Fib>> _fibs;
  private final @Nonnull ForwardingAnalysis _forwardingAnalysis;
  private final @Nonnull Table<String, String, Set<Layer2Vni>> _layer2Vnis;
  private final @Nonnull Table<String, String, Set<Layer3Vni>> _layer3Vnis;
  private final @Nonnull SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
      _prefixTracingInfoSummary;
  private final @Nonnull Table<String, String, FinalMainRib> _ribs;

  private static <T> Table<String, String, T> toTable(
      Map<String, PerHostDataPlane> perHostDataPlane,
      Function<PerHostDataPlane, Map<String, T>> getter) {
    ImmutableTable.Builder<String, String, T> ret = ImmutableTable.builder();
    perHostDataPlane.forEach(
        (hostname, hostDataPlane) ->
            getter.apply(hostDataPlane).forEach((key, t) -> ret.put(hostname, key, t)));
    return ret.build();
  }

  public SimpleFieldsDataPlane(
      @Nonnull Map<String, PerHostDataPlane> perHostDataPlanes,
      @Nonnull ForwardingAnalysis forwardingAnalysis) {
    _bgpRoutes = toTable(perHostDataPlanes, PerHostDataPlane::getBgpRoutes);
    _bgpBackupRoutes = toTable(perHostDataPlanes, PerHostDataPlane::getBgpBackupRoutes);
    _evpnRoutes = toTable(perHostDataPlanes, PerHostDataPlane::getEvpnRoutes);
    _evpnBackupRoutes = toTable(perHostDataPlanes, PerHostDataPlane::getEvpnBackupRoutes);
    _fibs =
        perHostDataPlanes.entrySet().parallelStream()
            .collect(ImmutableMap.toImmutableMap(Entry::getKey, e -> e.getValue().getFibs()));
    _forwardingAnalysis = forwardingAnalysis;
    _layer2Vnis = toTable(perHostDataPlanes, PerHostDataPlane::getLayer2Vnis);
    _layer3Vnis = toTable(perHostDataPlanes, PerHostDataPlane::getLayer3Vnis);
    _prefixTracingInfoSummary =
        perHostDataPlanes.entrySet().parallelStream()
            .collect(
                ImmutableSortedMap
                    .<Entry<String, PerHostDataPlane>, String,
                        SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
                        toImmutableSortedMap(
                            Ordering.natural(),
                            Entry::getKey,
                            e -> e.getValue().getPrefixTracingInfoSummary()));
    _ribs = toTable(perHostDataPlanes, PerHostDataPlane::getRibs);
  }

  @Override
  public @Nonnull Table<String, String, Set<Bgpv4Route>> getBgpRoutes() {
    return _bgpRoutes;
  }

  @Override
  public @Nonnull Table<String, String, Set<Bgpv4Route>> getBgpBackupRoutes() {
    return _bgpBackupRoutes;
  }

  @Override
  public @Nonnull Table<String, String, Set<EvpnRoute<?, ?>>> getEvpnRoutes() {
    return _evpnRoutes;
  }

  @Override
  public @Nonnull Table<String, String, Set<EvpnRoute<?, ?>>> getEvpnBackupRoutes() {
    return _evpnBackupRoutes;
  }

  @Override
  public @Nonnull Map<String, Map<String, Fib>> getFibs() {
    return _fibs;
  }

  @Override
  public @Nonnull ForwardingAnalysis getForwardingAnalysis() {
    return _forwardingAnalysis;
  }

  @Override
  public @Nonnull Table<String, String, Set<Layer2Vni>> getLayer2Vnis() {
    return _layer2Vnis;
  }

  @Override
  public @Nonnull Table<String, String, Set<Layer3Vni>> getLayer3Vnis() {
    return _layer3Vnis;
  }

  @Override
  public @Nonnull SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
      getPrefixTracingInfoSummary() {
    return _prefixTracingInfoSummary;
  }

  @Override
  public @Nonnull Table<String, String, FinalMainRib> getRibs() {
    return _ribs;
  }
}
