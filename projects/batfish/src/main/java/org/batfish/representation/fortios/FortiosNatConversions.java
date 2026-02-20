package org.batfish.representation.fortios;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.transformation.AssignIpAddressFromPool;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.Transformation;

/** Helper functions for generating VI NAT structures for {@link FortiosConfiguration}. */
public final class FortiosNatConversions {

  /**
   * Converts an {@link Ippool} to a source NAT {@link Transformation} that assigns source IPs from
   * the pool.
   *
   * @param ippool The IP pool to convert
   * @param w Warnings for reporting issues
   * @return The transformation, or empty if the pool cannot be converted
   */
  @VisibleForTesting
  public static Optional<Transformation> toSourceNatTransformation(
      Ippool ippool, Warnings w, String filename) {
    RangeSet<Ip> poolRanges = computeIpRanges(ippool, w);
    if (poolRanges.isEmpty()) {
      return Optional.empty();
    }

    AssignIpAddressFromPool step =
        new AssignIpAddressFromPool(
            org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT,
            IpField.SOURCE,
            poolRanges);

    return Optional.of(Transformation.always().apply(step).build());
  }

  /**
   * Computes the IP ranges for an {@link Ippool} based on its configuration.
   *
   * <p>FortiOS IP pools can be configured using:
   *
   * <ul>
   *   <li>startip/endip - explicit IP range
   *   <li>prefix/netmask - subnet-based pool
   *   <li>ge/le - prefix length filtering (for prefix-based pools)
   * </ul>
   */
  private static @Nonnull RangeSet<Ip> computeIpRanges(Ippool ippool, Warnings w) {
    Ip startIp = ippool.getStartip();
    Ip endIp = ippool.getEndip();
    Ip prefixIp = ippool.getPrefixIp();
    Ip prefixNetmask = ippool.getPrefixNetmask();

    // Case 1: startip/endip range
    if (startIp != null && endIp != null) {
      if (endIp.compareTo(startIp) < 0) {
        w.redFlagf(
            "IP pool %s has invalid range: startip %s > endip %s",
            ippool.getName(), startIp, endIp);
        return ImmutableRangeSet.of();
      }
      return ImmutableRangeSet.of(Range.closed(startIp, endIp));
    }

    // Case 2: prefix-based pool
    if (prefixIp != null && prefixNetmask != null) {
      if (!prefixNetmask.isValidNetmask1sLeading()) {
        w.redFlagf("IP pool %s has invalid netmask: %s", ippool.getName(), prefixNetmask);
        return ImmutableRangeSet.of();
      }

      Prefix prefix = Prefix.create(prefixIp, prefixNetmask);
      Ip poolStart = prefix.getStartIp();
      Ip poolEnd = prefix.getEndIp();

      // Apply ge/le filtering if specified
      Integer ge = ippool.getGe();
      Integer le = ippool.getLe();

      if (ge != null || le != null) {
        // Log a warning since ge/le filtering is not fully supported for IP pools
        w.redFlagf(
            "IP pool %s uses ge/le filtering (%s/%s) which is not fully supported; "
                + "using full prefix range %s",
            ippool.getName(), ge, le, prefix);
      }

      return ImmutableRangeSet.of(Range.closed(poolStart, poolEnd));
    }

    // No valid configuration
    if (startIp == null && endIp == null && prefixIp == null) {
      w.redFlagf(
          "IP pool %s has no IP range configuration (missing startip/endip or prefix)",
          ippool.getName());
    } else {
      w.redFlagf(
          "IP pool %s has incomplete configuration: startip=%s, endip=%s, prefix=%s, netmask=%s",
          ippool.getName(), startIp, endIp, prefixIp, prefixNetmask);
    }
    return ImmutableRangeSet.of();
  }

  /**
   * Computes the outgoing transformation for an interface based on policies that have NAT enabled
   * with IP pools.
   *
   * @param dstInterface The destination interface name
   * @param policies Map of policy number to Policy
   * @param ippools Map of IP pool name to Ippool
   * @param c The VI configuration
   * @param w Warnings
   * @param filename The vendor config filename
   * @return The transformation to apply, or null if none
   */
  public static @Nullable Transformation computeOutgoingTransformation(
      String dstInterface,
      Map<String, Policy> policies,
      Map<String, Ippool> ippools,
      org.batfish.datamodel.Configuration c,
      Warnings w,
      String filename) {

    // Find policies that:
    // 1. Have NAT enabled
    // 2. Have IP pool enabled
    // 3. Have this interface as destination
    // 4. Have pool names configured
    ImmutableList.Builder<Transformation> transformations = ImmutableList.builder();

    for (Policy policy : policies.values()) {
      if (!policyMatchesNatCriteria(policy, dstInterface)) {
        continue;
      }

      // Get the IP pools for this policy
      for (String poolName : policy.getPoolnames()) {
        Ippool pool = ippools.get(poolName);
        if (pool == null) {
          w.redFlagf("Policy %s references non-existent IP pool %s", policy.getNumber(), poolName);
          continue;
        }

        Optional<Transformation> transformation = toSourceNatTransformation(pool, w, filename);
        transformation.ifPresent(transformations::add);
      }
    }

    // Chain transformations: first matching transformation wins
    return chainTransformations(transformations.build());
  }

  /**
   * Checks if a policy matches the criteria for NAT transformation on the given destination
   * interface.
   */
  private static boolean policyMatchesNatCriteria(Policy policy, String dstInterface) {
    // Policy must be enabled
    if (policy.getStatusEffective() != Policy.Status.ENABLE) {
      return false;
    }

    // NAT must be enabled
    if (!Boolean.TRUE.equals(policy.getNat())) {
      return false;
    }

    // IP pool must be enabled
    if (!Boolean.TRUE.equals(policy.getIppool())) {
      return false;
    }

    // Must have pool names configured
    if (policy.getPoolnames().isEmpty()) {
      return false;
    }

    // Must match the destination interface
    return policy.getDstIntf() != null && policy.getDstIntf().contains(dstInterface);
  }

  /** Chains multiple transformations using orElse. */
  private static @Nullable Transformation chainTransformations(
      Collection<Transformation> transformations) {
    if (transformations.isEmpty()) {
      return null;
    }

    Transformation result = null;
    for (Transformation t : transformations) {
      if (result == null) {
        result = t;
      } else {
        // Chain: if current doesn't match, try the next one
        result =
            Transformation.when(t.getGuard())
                .apply(t.getTransformationSteps())
                .setOrElse(result)
                .build();
      }
    }
    return result;
  }

  private FortiosNatConversions() {} // prevent instantiation
}
