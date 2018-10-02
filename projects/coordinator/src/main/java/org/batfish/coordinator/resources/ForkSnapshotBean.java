package org.batfish.coordinator.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.batfish.common.CoordConstsV2;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.collections.NodeInterfacePair;

public class ForkSnapshotBean {
  /** Name of the snapshot to copy */
  @JsonProperty(CoordConstsV2.KEY_SNAPSHOT_BASE)
  public String baseSnapshot;

  /** List of interfaces to deactivate from the base snapshot */
  @JsonProperty(CoordConstsV2.KEY_DEACTIVATE_INTERFACES)
  public List<NodeInterfacePair> deactivateInterfaces;

  /** List of links to deactivate from the base snapshot */
  @JsonProperty(CoordConstsV2.KEY_DEACTIVATE_LINKS)
  public List<Edge> deactivateLinks;

  /** List of names of nodes to deactivate from the base snapshot */
  @JsonProperty(CoordConstsV2.KEY_DEACTIVATE_NODES)
  public List<String> deactivateNodes;

  @JsonCreator
  private ForkSnapshotBean() {}

  public ForkSnapshotBean(
      String baseSnapshotName,
      List<NodeInterfacePair> deactivateInterfacesList,
      List<Edge> deactivateLinksList,
      List<String> deactivateNodesList) {
    baseSnapshot = baseSnapshotName;
    deactivateInterfaces = deactivateInterfacesList;
    deactivateLinks = deactivateLinksList;
    deactivateNodes = deactivateNodesList;
  }
}
