package org.batfish.role;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;

/** Class that captures the node roles */
public class NodeRolesData {

  private static final String PROP_LAST_MODIFIED_TIME = "lastModifiedTime";

  private static final String PROP_ROLE_DIMENSIONS = "roleDimensions";

  @Nullable private Instant _lastModifiedTime;

  @Nonnull private SortedSet<NodeRoleDimension> _roleDimensions;

  @JsonCreator
  public NodeRolesData(
      @JsonProperty(PROP_LAST_MODIFIED_TIME) Instant lastModifiedTime,
      @JsonProperty(PROP_ROLE_DIMENSIONS) SortedSet<NodeRoleDimension> roleDimensions) {
    _lastModifiedTime = lastModifiedTime;
    _roleDimensions = roleDimensions == null ? new TreeSet<>() : roleDimensions;
  }

  @JsonProperty(PROP_LAST_MODIFIED_TIME)
  public Instant getLastModifiedTime() {
    return _lastModifiedTime;
  }

  @JsonProperty(PROP_ROLE_DIMENSIONS)
  public SortedSet<NodeRoleDimension> getNodeRoleDimensions() {
    return _roleDimensions;
  }

  /**
   * Reads the {@link NodeRolesData} object from the provided Path. If the path does not exist,
   * initializes a new object.
   *
   * @param dataPath The Path to read from
   * @return The read data
   * @throws IOException If file exists but its contents could not be cast to {@link NodeRolesData}
   */
  public static NodeRolesData read(Path dataPath) throws IOException {
    if (Files.exists(dataPath)) {
      String jsonStr = CommonUtil.readFile(dataPath);
      return BatfishObjectMapper.mapper().readValue(jsonStr, NodeRolesData.class);
    } else {
      return new NodeRolesData(new Date().toInstant(), null);
    }
  }

  /**
   * Replace all dimensions of a type with the provided data
   *
   * @param newDimensions The new role data. Null values are treated as if the map were empty.
   * @param roleType The type of roles to replace
   */
  public static synchronized void replaceNodeRoleDimensions(
      Path dataPath, SortedSet<NodeRoleDimension> newDimensions, NodeRoleDimension.Type roleType)
      throws IOException {

    NodeRolesData oldRolesData = read(dataPath);

    SortedSet<NodeRoleDimension> newRoles = new TreeSet<>();

    // in the new data, insert the type we don't want to replace from the old data
    newRoles.addAll(
        oldRolesData
            ._roleDimensions
            .stream()
            .filter(d -> roleType != d.getType())
            .collect(Collectors.toList()));

    // in the new data, get the type we want to replace from the new dimensions
    if (newDimensions != null) {
      newRoles.addAll(
          newDimensions.stream().filter(d -> roleType == d.getType()).collect(Collectors.toList()));
    }

    // this conditional write ensures that we update the last modified time only if needed
    if (!oldRolesData._roleDimensions.equals(newRoles)) {
      write(new NodeRolesData(new Date().toInstant(), newRoles), dataPath);
    }
  }

  public static synchronized void write(NodeRolesData data, Path dataPath)
      throws JsonProcessingException {
    CommonUtil.writeFile(dataPath, BatfishObjectMapper.writePrettyString(data));
  }

  /**
   * Get the {@link NodeRoleDimension} object for the specified dimension
   *
   * @param dataPath Path from where to read {@link NodeRolesData}
   * @param dimension The name of the dimension to fetch
   * @return The {@link NodeRoleDimension} object if one exists or throws {@link
   *     java.util.NoSuchElementException}
   * @throws IOException If the contents of the file could not be cast to {@link NodeRolesData}
   */
  public static NodeRoleDimension getNodeRoleDimensionByName(Path dataPath, String dimension)
      throws IOException {
    NodeRolesData data = read(dataPath);
    return data._roleDimensions
        .stream()
        .filter(d -> d.getName().equals(dimension))
        .findFirst()
        .get();
  }
}
