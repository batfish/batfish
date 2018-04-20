package org.batfish.role;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;

/** Class that captures the node roles */
public class NodeRolesData {

  public static enum NodeRoleType {
    AUTO,
    CUSTOM
  }

  public static final String AUTO_ROLES_PREFIX = ".auto";

  private static final String PROP_LAST_MODIFIED_TIME = "lastModifiedTime";

  private static final String PROP_ROLE_DIMENSIONS = "roleDimensions";

  @Nullable private Instant _lastModifiedTime;

  @Nonnull private Map<String, NodeRoleDimension> _roleDimensions;

  @JsonCreator
  public NodeRolesData(
      @JsonProperty(PROP_LAST_MODIFIED_TIME) Instant lastModifiedTime,
      @JsonProperty(PROP_ROLE_DIMENSIONS) Map<String, NodeRoleDimension> roleDimensions) {
    _lastModifiedTime = lastModifiedTime;
    _roleDimensions = roleDimensions == null ? new TreeMap<>() : roleDimensions;
  }

  @JsonProperty(PROP_LAST_MODIFIED_TIME)
  public Instant getLastModifiedTime() {
    return _lastModifiedTime;
  }

  @JsonProperty(PROP_ROLE_DIMENSIONS)
  public Map<String, NodeRoleDimension> getNodeRoleDimensions() {
    return _roleDimensions;
  }

  /**
   * Is the provided dimension name an auto-inferred dimension?
   *
   * @param dimension The name to test
   * @return The result
   */
  public static boolean isAuto(String dimension) {
    return dimension.startsWith(AUTO_ROLES_PREFIX);
  }

  /**
   * Is the the provided dimension of the provided type?
   *
   * @param dimension The dimention to test
   * @param type The type to test
   * @return The result
   */
  public static boolean isType(String dimension, NodeRoleType type) {
    return (type == NodeRoleType.AUTO && isAuto(dimension))
        || (type == NodeRoleType.CUSTOM && !isAuto(dimension));
  }

  /**
   * Reads the NodeRolesData from the provided Path. If the path does not exist, initializes a new
   * object.
   *
   * @param dataPath The Path to read from
   * @return The read data
   * @throws IOException
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
      Path dataPath, Map<String, NodeRoleDimension> newDimensions, NodeRoleType roleType)
      throws IOException {

    NodeRolesData oldRolesData = read(dataPath);

    Map<String, NodeRoleDimension> newRoles = new HashMap<>();

    // in the new data, get the type we don't want to replace from the old data
    newRoles.putAll(
        oldRolesData
            ._roleDimensions
            .entrySet()
            .stream()
            .filter(e -> !isType(e.getKey(), roleType))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

    // in the new data, get the type we want to replace from the new dimensions
    if (newDimensions != null) {
      newRoles.putAll(
          newDimensions
              .entrySet()
              .stream()
              .filter(e -> isType(e.getKey(), roleType))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
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
}
