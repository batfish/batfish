package org.batfish.bddreachability;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibForward;
import org.batfish.datamodel.FibNextVrf;
import org.batfish.datamodel.FibNullRoute;
import org.batfish.datamodel.FibVtep;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.visitors.FibActionVisitor;

/**
 * Generates {@link IpsRoutedOutInterfaces} objects for a {@link Fib} (identified by node/vrf
 * names). Each object provides the set of dest IPs (represented as a {@link BDD}) for which a given
 * interface is chosen as the egress interface to route traffic out through.
 */
public final class IpsRoutedOutInterfacesFactory {
  private final Map<String, Map<String, Fib>> _fibs;

  static class IpsRoutedOutInterfaces {
    // initialize lazily
    private Map<String, IpSpace> _map = null;
    private final Fib _fib;

    IpsRoutedOutInterfaces(Fib fib) {
      _fib = fib;
    }

    /**
     * @param iface The name of an interface
     * @return a {@link BDD} representing the set of dest IPs routed out {@code iface}.
     */
    @Nonnull
    IpSpace getIpsRoutedOutInterface(String iface) {
      if (_map == null) {
        _map = computeIpsRoutedOutInterfacesMap(_fib);
      }
      return _map.getOrDefault(iface, EmptyIpSpace.INSTANCE);
    }
  }

  public IpsRoutedOutInterfacesFactory(Map<String, Map<String, Fib>> fibs) {
    _fibs = fibs;
  }

  IpsRoutedOutInterfaces getIpsRoutedOutInterfaces(String node, String vrf) {
    return new IpsRoutedOutInterfaces(_fibs.get(node).get(vrf));
  }

  private static class ResolvedInterface implements FibActionVisitor<Optional<String>> {
    public static final ResolvedInterface INSTANCE = new ResolvedInterface();

    @Override
    public Optional<String> visitFibForward(FibForward fibForward) {
      return Optional.of(fibForward.getInterfaceName());
    }

    @Override
    public Optional<String> visitFibNextVrf(FibNextVrf fibNextVrf) {
      return Optional.empty();
    }

    @Override
    public Optional<String> visitFibNullRoute(FibNullRoute fibNullRoute) {
      return Optional.empty();
    }

    @Override
    public Optional<String> visitFibVtep(FibVtep fibVtep) {
      return Optional.empty();
    }

    private ResolvedInterface() {}
  }

  @VisibleForTesting
  static Map<String, IpSpace> computeIpsRoutedOutInterfacesMap(Fib fib) {
    Map<Prefix, IpSpace> matchingIps = fib.getMatchingIps();
    return fib.allEntries().stream()
        .filter(fibEntry -> fibEntry.getAction().accept(ResolvedInterface.INSTANCE).isPresent())
        .collect(
            groupingBy(
                fibEntry -> fibEntry.getAction().accept(ResolvedInterface.INSTANCE).get(),
                mapping(
                    fibEntry -> matchingIps.get(fibEntry.getTopLevelRoute().getNetwork()),
                    collectingAndThen(
                        Collectors.toList(),
                        ipSpaces ->
                            firstNonNull(AclIpSpace.union(ipSpaces), EmptyIpSpace.INSTANCE)))));
  }
}
