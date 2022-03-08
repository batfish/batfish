package org.batfish.dataplane.ibdp;

import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.topology.IpOwnersBaseImpl;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.datamodel.Configuration;
import org.batfish.dataplane.ibdp.DataplaneTrackEvaluator.DataPlaneTrackMethodEvaluatorProvider;

/**
 * {@link org.batfish.common.topology.IpOwners} implementation that uses information from a complete
 * or partial data plane.{@link DataPlaneTrackMethodEvaluatorProvider}.
 */
@ParametersAreNonnullByDefault
final class DataPlaneIpOwners extends IpOwnersBaseImpl {

  /**
   * Construct a {@link DataPlaneIpOwners} from configurations, data-plane-based {@link
   * L3Adjacencies}, and a {@link DataPlaneTrackMethodEvaluatorProvider}.
   */
  public DataPlaneIpOwners(
      Map<String, Configuration> configurations,
      L3Adjacencies l3Adjacencies,
      DataPlaneTrackMethodEvaluatorProvider trackMethodEvaluatorProvider) {
    super(configurations, l3Adjacencies, trackMethodEvaluatorProvider, false);
  }
}
