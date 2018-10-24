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
  private static final String PROP_RESTORE_INTERFACES = "restoreInterfaces";
  private static final String PROP_RESTORE_LINKS = "restoreLinks";
  private static final String PROP_RESTORE_NODES = "restoreNodes";
  private static final String PROP_ZIP_FILE = "zipFile";

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

  /** List of interfaces to restore (undo deactivation) */
  @JsonProperty(PROP_RESTORE_INTERFACES)
  public List<NodeInterfacePair> restoreInterfaces;

  /** List of links to restore (undo deactivation) */
  @JsonProperty(PROP_RESTORE_LINKS)
  public List<Edge> restoreLinks;

  /** List of names of nodes to restore (undo deactivation) */
  @JsonProperty(PROP_RESTORE_NODES)
  public List<String> restoreNodes;

  /**
   * Zip file, containing files to write to new snapshot input dir (same format as initial
   * snapshot-upload-zips)
   */
  @JsonProperty(PROP_ZIP_FILE)
  public byte[] zipFile;

  @JsonCreator
  private ForkSnapshotBean() {}

  public ForkSnapshotBean(
      String baseSnapshotName,
      String newSnapshotName,
      List<NodeInterfacePair> deactivateInterfacesList,
      List<Edge> deactivateLinksList,
      List<String> deactivateNodesList,
      List<NodeInterfacePair> restoreInterfacesList,
      List<Edge> restoreLinksList,
      List<String> restoreNodesList,
      byte[] file) {
    baseSnapshot = baseSnapshotName;
    newSnapshot = newSnapshotName;
    deactivateInterfaces = deactivateInterfacesList;
    deactivateLinks = deactivateLinksList;
    deactivateNodes = deactivateNodesList;
    restoreInterfaces = restoreInterfacesList;
    restoreLinks = restoreLinksList;
    restoreNodes = restoreNodesList;
    zipFile = file;
  }
}
