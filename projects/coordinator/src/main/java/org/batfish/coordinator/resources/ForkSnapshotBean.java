package org.batfish.coordinator.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.collections.NodeInterfacePair;

public class ForkSnapshotBean {
  private static final String PROP_SNAPSHOT_BASE = "snapshotBase";
  private static final String PROP_SNAPSHOT_NEW = "snapshotNew";
  private static final String PROP_DEACTIVATE_INTERFACES = "deactivateInterfaces";
  private static final String PROP_DEACTIVATE_LINKS = "deactivateLinks";
  private static final String PROP_DEACTIVATE_NODES = "deactivateNodes";

  /** Name of the snapshot to copy */
  @JsonProperty(PROP_SNAPSHOT_BASE)
  public String baseSnapshot;

  /** Name of the snapshot to create */
  @JsonProperty(PROP_SNAPSHOT_NEW)
  public String newSnapshot;

  /** List of interfaces to deactivate */
  @JsonProperty(PROP_DEACTIVATE_INTERFACES)
  public List<NodeInterfacePair> deactivateInterfaces;

  /** List of links to deactivate */
  @JsonProperty(PROP_DEACTIVATE_LINKS)
  public List<Edge> deactivateLinks;

  /** List of names of nodes to deactivate */
  @JsonProperty(PROP_DEACTIVATE_NODES)
  public List<String> deactivateNodes;

  @JsonCreator
  private ForkSnapshotBean() {}

  public ForkSnapshotBean(
      String baseSnapshotName,
      String newSnapshotName,
      List<NodeInterfacePair> deactivateInterfacesList,
      List<Edge> deactivateLinksList,
      List<String> deactivateNodesList) {
    baseSnapshot = baseSnapshotName;
    newSnapshot = newSnapshotName;
    deactivateInterfaces = deactivateInterfacesList;
    deactivateLinks = deactivateLinksList;
    deactivateNodes = deactivateNodesList;
  }
}
