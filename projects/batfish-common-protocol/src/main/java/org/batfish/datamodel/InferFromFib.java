package org.batfish.datamodel;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.visitors.FibActionVisitor;

/**
 * An implementation of {@link SourceIpInference} that infers source IP from FIB -- find LPM routes
 * for the destination IP of the packet, and returns the IPs of those routes' forwarding interfaces.
 * This is the default behavior in most cases.
 */
public final class InferFromFib extends SourceIpInference {
  private static final InferFromFib INSTANCE = new InferFromFib();

  /** Returns the singleton instance of {@link InferFromFib}. */
  public static @Nonnull InferFromFib instance() {
    return INSTANCE;
  }

  private InferFromFib() {}

  /**
   * Returns the potential source IPs of a packet with the given {@code dstIp} originating on the
   * given {@link Configuration} in a VRF with the given {@link Fib}. Concretely, find LPM routes
   * for {@code dstIp} and returns the IPs of those routes' forwarding interfaces.
   */
  @Override
  public Set<Ip> getPotentialSourceIps(
      @Nonnull Ip dstIp, @Nullable Fib fib, @Nonnull Configuration c) {
    if (fib == null) {
      return ImmutableSet.of();
    }
    return getSrcInterfaces(dstIp, fib, c).stream()
        .map(Interface::getConcreteAddress)
        .filter(Objects::nonNull)
        .map(ConcreteInterfaceAddress::getIp)
        .collect(ImmutableSet.toImmutableSet());
  }

  /**
   * Returns the names of source interface(s) of a packet with the given {@code dstIp} originating
   * on the given {@link Configuration} in a VRF with the given {@link Fib}. Concretely, finds LPM
   * routes for {@code dstIp} and returns those routes' forwarding interfaces.
   */
  private static Set<Interface> getSrcInterfaces(
      @Nonnull Ip dstIp, @Nonnull Fib fib, @Nonnull Configuration c) {
    return fib.get(dstIp).stream()
        .map(FibEntry::getAction)
        // Find forwarding interface for this FIB entry, if any
        .map(
            action ->
                action.accept(
                    new FibActionVisitor<String>() {
                      @Override
                      public String visitFibForward(FibForward fibForward) {
                        return fibForward.getInterfaceName();
                      }

                      @Override
                      public String visitFibNextVrf(FibNextVrf fibNextVrf) {
                        // TODO Can BGP peers initiate via interfaces in other VRFs? If
                        //  so, need to return such interfaces here.
                        return null;
                      }

                      @Override
                      public String visitFibNullRoute(FibNullRoute fibNullRoute) {
                        return null;
                      }
                    }))
        .filter(Objects::nonNull)
        .map(forwardingIfaceName -> c.getActiveInterfaces().get(forwardingIfaceName))
        .filter(Objects::nonNull)
        .collect(ImmutableSet.toImmutableSet());
  }
}
