package org.batfish.storage;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FinalMainRib;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.datamodel.vxlan.Layer3Vni;

final class PerHostDataPlane implements Serializable {
  private final @Nonnull Map<String, Set<Bgpv4Route>> _bgpRoutes;
  private final @Nonnull Map<String, Set<Bgpv4Route>> _bgpBackupRoutes;
  private final @Nonnull Map<String, Set<EvpnRoute<?, ?>>> _evpnRoutes;
  private final @Nonnull Map<String, Set<EvpnRoute<?, ?>>> _evpnBackupRoutes;
  private final @Nonnull Map<String, Fib> _fibs;
  private final @Nonnull Map<String, Set<Layer2Vni>> _layer2Vnis;
  private final @Nonnull Map<String, Set<Layer3Vni>> _layer3Vnis;
  private final @Nonnull SortedMap<String, Map<Prefix, Map<String, Set<String>>>>
      _prefixTracingInfoSummary;
  private final @Nonnull Map<String, FinalMainRib> _ribs;

  public PerHostDataPlane(
      @Nonnull Map<String, Set<Bgpv4Route>> bgpRoutes,
      @Nonnull Map<String, Set<Bgpv4Route>> bgpBackupRoutes,
      @Nonnull Map<String, Set<EvpnRoute<?, ?>>> evpnRoutes,
      @Nonnull Map<String, Set<EvpnRoute<?, ?>>> evpnBackupRoutes,
      @Nonnull Map<String, Fib> fibs,
      @Nonnull Map<String, Set<Layer2Vni>> layer2Vnis,
      @Nonnull Map<String, Set<Layer3Vni>> layer3Vnis,
      @Nonnull SortedMap<String, Map<Prefix, Map<String, Set<String>>>> prefixTracingInfoSummary,
      @Nonnull Map<String, FinalMainRib> ribs) {
    _bgpRoutes = bgpRoutes;
    _bgpBackupRoutes = bgpBackupRoutes;
    _evpnRoutes = evpnRoutes;
    _evpnBackupRoutes = evpnBackupRoutes;
    _fibs = fibs;
    _layer2Vnis = layer2Vnis;
    _layer3Vnis = layer3Vnis;
    _prefixTracingInfoSummary = prefixTracingInfoSummary;
    _ribs = ribs;
  }

  public @Nonnull Map<String, Set<Bgpv4Route>> getBgpRoutes() {
    return _bgpRoutes;
  }

  public @Nonnull Map<String, Set<Bgpv4Route>> getBgpBackupRoutes() {
    return _bgpBackupRoutes;
  }

  public @Nonnull Map<String, Set<EvpnRoute<?, ?>>> getEvpnRoutes() {
    return _evpnRoutes;
  }

  public @Nonnull Map<String, Set<EvpnRoute<?, ?>>> getEvpnBackupRoutes() {
    return _evpnBackupRoutes;
  }

  public @Nonnull Map<String, Fib> getFibs() {
    return _fibs;
  }

  public @Nonnull Map<String, Set<Layer2Vni>> getLayer2Vnis() {
    return _layer2Vnis;
  }

  public @Nonnull Map<String, Set<Layer3Vni>> getLayer3Vnis() {
    return _layer3Vnis;
  }

  public @Nonnull SortedMap<String, Map<Prefix, Map<String, Set<String>>>>
      getPrefixTracingInfoSummary() {
    return _prefixTracingInfoSummary;
  }

  public @Nonnull Map<String, FinalMainRib> getRibs() {
    return _ribs;
  }
}
