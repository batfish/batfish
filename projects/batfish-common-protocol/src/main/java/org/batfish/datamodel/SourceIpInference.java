package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.visitors.FibActionVisitor;

/** Abstract class for source IP inference of locally generated IP packets. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = SourceIpInference.InferFromFib.class, name = "inferFromFib"),
  @JsonSubTypes.Type(value = SourceIpInference.UseConstantIp.class, name = "useConstantIp"),
})
public abstract class SourceIpInference implements Serializable {
  /**
   * Returns the potential source IPs of a packet with the given {@code dstIp} originating on the
   * given {@link Configuration} in a VRF with the given {@link Fib}.
   */
  public abstract Set<Ip> getPotentialSourceIps(Ip dstIp, Fib fib, Configuration c);

  /**
   * Infer source IP from FIB -- find LPM routes for the destination IP of the packet, and returns
   * the IPs of those routes' forwarding interfaces. This is the default behavior in most cases.
   */
  public static final class InferFromFib extends SourceIpInference {
    private static final InferFromFib INSTANCE = new InferFromFib();

    /** Returns the singleton instance of {@link InferFromFib}. */
    @JsonValue
    @JsonCreator
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
    public Set<Ip> getPotentialSourceIps(Ip dstIp, Fib fib, Configuration c) {
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
    private static Set<Interface> getSrcInterfaces(Ip dstIp, Fib fib, Configuration c) {
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

  /** Use a constant source IP regardless of destination IP and outgoing interface. */
  public static final class UseConstantIp extends SourceIpInference {
    private static final String PROP_IP = "ip";

    @Nonnull private final Ip _ip;

    /** Return a singleton set of the given {@code ip}. */
    @Override
    public Set<Ip> getPotentialSourceIps(Ip dstIp, Fib fib, Configuration c) {
      return ImmutableSet.of(_ip);
    }

    private UseConstantIp(@Nonnull Ip ip) {
      _ip = ip;
    }

    /** Factory for creating a {@link UseConstantIp}. */
    @JsonCreator
    public static @Nonnull UseConstantIp create(@JsonProperty(PROP_IP) @Nonnull Ip ip) {
      return new UseConstantIp(ip);
    }

    @Nonnull
    public Ip getIp() {
      return _ip;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof UseConstantIp)) {
        return false;
      }
      UseConstantIp useConstantIp = (UseConstantIp) o;
      return _ip.equals(useConstantIp._ip);
    }

    @Override
    public int hashCode() {
      return _ip.hashCode();
    }
  }
}
