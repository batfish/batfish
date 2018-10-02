package org.batfish.role;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.role.NodeRoleDimension.Type;

/** Class that captures the node roles */
public class NodeRolesData {

  private static final String PROP_DEFAULT_DIMENSION = "defaultDimension";
  private static final String PROP_LAST_MODIFIED_TIME = "lastModifiedTime";
  private static final String PROP_ROLE_DIMENSIONS = "roleDimensions";

  @Nullable private String _defaultDimension;

  @Nullable private Instant _lastModifiedTime;

  @Nonnull private SortedSet<NodeRoleDimension> _roleDimensions;

  @JsonCreator
  public NodeRolesData(
      @JsonProperty(PROP_DEFAULT_DIMENSION) String defaultDimension,
      @JsonProperty(PROP_LAST_MODIFIED_TIME) Instant lastModifiedTime,
      @JsonProperty(PROP_ROLE_DIMENSIONS) SortedSet<NodeRoleDimension> roleDimensions) {
    _defaultDimension = defaultDimension;
    _lastModifiedTime = lastModifiedTime;
    _roleDimensions = roleDimensions == null ? new TreeSet<>() : roleDimensions;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NodeRolesData)) {
      return false;
    }
    return Objects.equals(_defaultDimension, ((NodeRolesData) o)._defaultDimension)
        && Objects.equals(_lastModifiedTime, ((NodeRolesData) o)._lastModifiedTime)
        && Objects.equals(_roleDimensions, ((NodeRolesData) o)._roleDimensions);
  }

  @JsonProperty(PROP_DEFAULT_DIMENSION)
  public String getDefaultDimension() {
    return _defaultDimension;
  }

  @JsonProperty(PROP_LAST_MODIFIED_TIME)
  public Instant getLastModifiedTime() {
    return _lastModifiedTime;
  }

  /** Returns the specified dimension in this NodeRolesData object */
  public Optional<NodeRoleDimension> getNodeRoleDimension(String dimension) {
    return _roleDimensions.stream().filter(d -> d.getName().equals(dimension)).findFirst();
  }

  /**
   * Get the {@link NodeRoleDimension} object for the specified dimension. If dimension is null,
   * returns {@link #getNodeRoleDimension(Path)}.
   *
   * @param dataPath Path from where to read {@link NodeRolesData}
   * @param dimension The name of the dimension to fetch
   * @return The {@link NodeRoleDimension} object if one exists or throws {@link
   *     java.util.NoSuchElementException} if {@code dimension} is non-null and not found.
   * @throws IOException If the contents of the file could not be cast to {@link NodeRolesData}
   */
  public static Optional<NodeRoleDimension> getNodeRoleDimension(
      Supplier<NodeRolesData> read, String dimension) throws IOException {
    if (dimension == null) {
      return getNodeRoleDimension(read);
    }
    NodeRolesData data = read.get();
    return data.getNodeRoleDimension(dimension);
  }

  /**
   * Get some "reasonable" {@link NodeRoleDimension} object for analysis. Preference order: the
   * default dimension if set and exists, the auto-inferred primary dimension if it exists, the
   * dimension that is lexicographically first, and null if no dimensions exist.
   *
   * @param dataPath Path from where to read {@link NodeRolesData}
   * @throws IOException If the contents of the file could not be cast to {@link NodeRolesData}
   */
  @Nullable
  private static Optional<NodeRoleDimension> getNodeRoleDimension(Supplier<NodeRolesData> read)
      throws IOException {
    NodeRolesData data = read.get();
    // check default
    if (data.getDefaultDimension() != null) {
      Optional<NodeRoleDimension> opt = data.getNodeRoleDimension(data.getDefaultDimension());
      if (opt.isPresent()) {
        return opt;
      }
    }
    // check auto primary
    Optional<NodeRoleDimension> optAuto =
        data.getNodeRoleDimension(NodeRoleDimension.AUTO_DIMENSION_PRIMARY);
    if (optAuto.isPresent()) {
      return optAuto;
    }
    // check first
    return data.getNodeRoleDimensions()
        .stream()
        .min(Comparator.comparing(NodeRoleDimension::getName));
  }

  @JsonProperty(PROP_ROLE_DIMENSIONS)
  public SortedSet<NodeRoleDimension> getNodeRoleDimensions() {
    return _roleDimensions;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_defaultDimension, _lastModifiedTime, _roleDimensions);
  }

  /**
   * Merge the dimensions in current file with new data. If the same dimension is present in both
   * data sources, the new data wins. If defaultDimension is non-null, it is deemed as the default
   * dimension. Optionally, delete all dimensions of type AUTO before adding new data.
   *
   * @param dataPath Location of the old data
   * @param newDimensions The new role data. Null values are treated as if the map were empty.
   * @param defaultDimension Dimension to deem as default after merger
   * @param deleteAutoFirst If dimensions of type AUTO should be deleted first
   */
  public static synchronized void mergeNodeRoleDimensions(
      Supplier<NodeRolesData> read,
      Consumer<NodeRolesData> write,
      SortedSet<NodeRoleDimension> newDimensions,
      String defaultDimension,
      boolean deleteAutoFirst)
      throws IOException {

    NodeRolesData oldRolesData = read.get();

    final SortedSet<NodeRoleDimension> finalNewDimensions =
        newDimensions == null ? new TreeSet<>() : newDimensions;

    // add the old role dimensions that are not in common with new dimensions
    SortedSet<NodeRoleDimension> newRoles =
        new TreeSet<>(
            oldRolesData
                ._roleDimensions
                .stream()
                .filter(d -> !finalNewDimensions.contains(d))
                .collect(Collectors.toSet()));

    // delete the auto dimensions if needed
    if (deleteAutoFirst) {
      newRoles.removeIf(d -> d.getType() == Type.AUTO);
    }

    // add the new dimensions
    newRoles.addAll(finalNewDimensions);

    write.accept(
        new NodeRolesData(
            defaultDimension == null ? oldRolesData.getDefaultDimension() : defaultDimension,
            new Date().toInstant(),
            newRoles));
  }
}
