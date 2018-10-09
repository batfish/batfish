package org.batfish.coordinator.resources;

import static org.batfish.common.CoordConstsV2.RSC_ENV_TOPOLOGY;
import static org.batfish.common.CoordConstsV2.RSC_EXTENDED;
import static org.batfish.common.CoordConstsV2.RSC_INPUT;
import static org.batfish.common.CoordConstsV2.RSC_METADATA;
import static org.batfish.common.CoordConstsV2.RSC_POJO_TOPOLOGY;

import io.opentracing.util.GlobalTracer;
import java.io.FileNotFoundException;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.batfish.coordinator.Main;

/** Resource for servicing client API calls for a specific snapshot */
@ParametersAreNonnullByDefault
public final class SnapshotResource {

  private final String _network;

  private final String _snapshot;

  public SnapshotResource(String network, String snapshot) {
    _network = network;
    _snapshot = snapshot;
  }

  /**
   * Fork the specified snapshot and make changes to the new snapshot
   *
   * @param forkSnapshotBean The {@link ForkSnapshotBean} containing parameters used to create the
   *     fork
   */
  @PUT
  public Response forkSnapshot(ForkSnapshotBean forkSnapshotBean) {
    try {
      if (GlobalTracer.get().activeSpan() != null) {
        GlobalTracer.get()
            .activeSpan()
            .setTag("network-name", _network)
            .setTag("snapshot-name", _snapshot);
      }

      Main.getWorkMgr().forkSnapshot(_network, _snapshot, forkSnapshotBean);
      return Response.ok().build();
    } catch (FileNotFoundException e) {
      return Response.status(Status.NOT_FOUND).entity(e.getMessage()).build();
    } catch (Exception e) {
      return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
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
