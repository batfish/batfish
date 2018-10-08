package org.batfish.coordinator.resources;

import static org.batfish.common.CoordConstsV2.RSC_ENV_TOPOLOGY;
import static org.batfish.common.CoordConstsV2.RSC_EXTENDED;
import static org.batfish.common.CoordConstsV2.RSC_INPUT;
import static org.batfish.common.CoordConstsV2.RSC_METADATA;
import static org.batfish.common.CoordConstsV2.RSC_POJO_TOPOLOGY;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.Path;

/** Resource for servicing client API calls for a specific snapshot */
@ParametersAreNonnullByDefault
public final class SnapshotResource {

  private final String _network;

  private final String _snapshot;

  public SnapshotResource(String network, String snapshot) {
    _network = network;
    _snapshot = snapshot;
  }

  @Path(RSC_ENV_TOPOLOGY)
  public SnapshotEnvTopologyResource getSnapshotEnvTopologyResource() {
    return new SnapshotEnvTopologyResource(_network, _snapshot);
  }

  @Path(RSC_EXTENDED)
  public SnapshotExtendedObjectsResource getSnapshotExtendedObjectsResource() {
    return new SnapshotExtendedObjectsResource(_network, _snapshot);
  }

  @Path(RSC_INPUT)
  public SnapshotInputObjectsResource getSnapshotInputObjectsResource() {
    return new SnapshotInputObjectsResource(_network, _snapshot);
  }

  @Path(RSC_METADATA)
  public SnapshotMetadataResource getSnapshotMetadataResource() {
    return new SnapshotMetadataResource(_network, _snapshot);
  }

  @Path(RSC_POJO_TOPOLOGY)
  public SnapshotPojoTopologyResource getSnapshotPojoTopologyResource() {
    return new SnapshotPojoTopologyResource(_network, _snapshot);
  }
}
