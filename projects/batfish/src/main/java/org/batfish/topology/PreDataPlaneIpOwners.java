package org.batfish.topology;

import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.topology.IpOwners;
import org.batfish.common.topology.IpOwnersBaseImpl;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.tracking.PreDataPlaneTrackMethodEvaluator;

/**
 * {@Link IpOwners} implementation using only pre-dataplane information, i.e. configs and initial
 * {@link L3Adjacencies}.
 *
 * <p>The following differences may result versus post-dataplane {@link IpOwners}:
 *
 * <ul>
 *   <li>There may be more HSRP/VRRP winners (more elections with fewer candidates) compared to
 *       post-dataplane, since initial {@link L3Adjacencies} are expected to be a subset of
 *       post-dataplane {@link L3Adjacencies}.
 *   <li>HSRP/VRRP priority tracks based on reachability or route presence are assumed to fail. In
 *       realistic cases, this would result in more ties versus post-dataplane {@link IpOwners}.
 *       Such ties may be broken using arbitrary criteria such as highest source address (RFC) or
 *       node/interface name.
 * </ul>
 */
@ParametersAreNonnullByDefault
final class PreDataPlaneIpOwners extends IpOwnersBaseImpl {

  PreDataPlaneIpOwners(Map<String, Configuration> configurations, L3Adjacencies l3Adjacencies) {
    super(configurations, l3Adjacencies, PreDataPlaneTrackMethodEvaluator::new, false);
  }
}
