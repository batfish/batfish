package org.batfish.coordinator.resources;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
  public final String baseSnapshot;

  /** Name of the snapshot to create */
  @JsonProperty(PROP_SNAPSHOT_NEW)
  public final String newSnapshot;

  /** List of interfaces to deactivate */
  @JsonProperty(PROP_DEACTIVATE_INTERFACES)
  public final List<NodeInterfacePair> deactivateInterfaces;

  /** List of links to deactivate */
  @JsonProperty(PROP_DEACTIVATE_LINKS)
  public final List<Edge> deactivateLinks;

  /** List of names of nodes to deactivate */
  @JsonProperty(PROP_DEACTIVATE_NODES)
  public final List<String> deactivateNodes;

  /** List of interfaces to restore (undo deactivation) */
  @JsonProperty(PROP_RESTORE_INTERFACES)
  public final List<NodeInterfacePair> restoreInterfaces;

  /** List of links to restore (undo deactivation) */
  @JsonProperty(PROP_RESTORE_LINKS)
  public final List<Edge> restoreLinks;

  /** List of names of nodes to restore (undo deactivation) */
  @JsonProperty(PROP_RESTORE_NODES)
  public final List<String> restoreNodes;

  /**
   * Zip file, containing files to write to new snapshot input dir (same format as initial
   * snapshot-upload-zips)
   */
  @JsonProperty(PROP_ZIP_FILE)
  public final byte[] zipFile;

  @JsonCreator
  private static ForkSnapshotBean create(
      @JsonProperty(PROP_SNAPSHOT_BASE) @Nullable String baseSnapshotName,
      @JsonProperty(PROP_SNAPSHOT_NEW) @Nullable String newSnapshotName,
      @JsonProperty(PROP_DEACTIVATE_INTERFACES) @Nullable
          List<NodeInterfacePair> deactivateInterfacesList,
      @JsonProperty(PROP_DEACTIVATE_LINKS) @Nullable List<Edge> deactivateLinksList,
      @JsonProperty(PROP_DEACTIVATE_NODES) @Nullable List<String> deactivateNodesList,
      @JsonProperty(PROP_RESTORE_INTERFACES) @Nullable
          List<NodeInterfacePair> restoreInterfacesList,
      @JsonProperty(PROP_RESTORE_LINKS) @Nullable List<Edge> restoreLinksList,
      @JsonProperty(PROP_RESTORE_NODES) @Nullable List<String> restoreNodesList,
      @JsonProperty(PROP_ZIP_FILE) @Nullable byte[] file) {
    checkArgument(baseSnapshotName != null, "Base snapshot name cannot be null");
    checkArgument(newSnapshotName != null, "New snapshot name cannot be null");
    return new ForkSnapshotBean(
        baseSnapshotName,
        newSnapshotName,
        deactivateInterfacesList,
        deactivateLinksList,
        deactivateNodesList,
        restoreInterfacesList,
        restoreLinksList,
        restoreNodesList,
        file);
  }

  public ForkSnapshotBean(
      @Nonnull String baseSnapshotName,
      @Nonnull String newSnapshotName,
      @Nullable List<NodeInterfacePair> deactivateInterfacesList,
      @Nullable List<Edge> deactivateLinksList,
      @Nullable List<String> deactivateNodesList,
      @Nullable List<NodeInterfacePair> restoreInterfacesList,
      @Nullable List<Edge> restoreLinksList,
      @Nullable List<String> restoreNodesList,
      @Nullable byte[] file) {
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
