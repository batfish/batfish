package org.batfish.config;

import static com.google.common.base.Preconditions.checkState;

import java.nio.file.Path;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.common.BfConsts;
import org.batfish.identifiers.SnapshotId;

public final class TestrigSettings {

  private Path _basePath;

  private SnapshotId _name;

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof TestrigSettings)) {
      return false;
    }
    TestrigSettings other = (TestrigSettings) obj;
    return _name.equals(other._name);
  }

  @Nonnull
  public Path getBasePath() {
    checkState(_basePath != null, "base path is not configured");
    return _basePath;
  }

  @Nonnull
  public Path getDataPlanePath() {
    return getOutputPath().resolve(BfConsts.RELPATH_DATA_PLANE);
  }

  public Path getDataPlaneAnswerPath() {
    return getOutputPath().resolve(BfConsts.RELPATH_DATA_PLANE_ANSWER_PATH);
  }

  public Path getEnvironmentBgpTablesPath() {
    return getInputPath().resolve(BfConsts.RELPATH_ENVIRONMENT_BGP_TABLES);
  }

  public Path getEnvironmentRoutingTablesPath() {
    return getInputPath().resolve(BfConsts.RELPATH_ENVIRONMENT_ROUTING_TABLES);
  }

  public Path getExternalBgpAnnouncementsPath() {
    return getInputPath().resolve(BfConsts.RELPATH_EXTERNAL_BGP_ANNOUNCEMENTS);
  }

  public Path getInferredNodeRolesPath() {
    return getOutputPath().resolve(BfConsts.RELPATH_INFERRED_NODE_ROLES_PATH);
  }

  public Path getInputPath() {
    return getBasePath().resolve(BfConsts.RELPATH_INPUT);
  }

  public SnapshotId getName() {
    return _name;
  }

  public Path getNodeRolesPath() {
    return getInputPath().resolve(BfConsts.RELPATH_NODE_ROLES_PATH);
  }

  public Path getOutputPath() {
    return getBasePath().resolve(BfConsts.RELPATH_OUTPUT);
  }

  public Path getParseAnswerPath() {
    return getOutputPath().resolve(BfConsts.RELPATH_PARSE_ANSWER_PATH);
  }

  public Path getReferenceLibraryPath() {
    return getInputPath().resolve(BfConsts.RELPATH_REFERENCE_LIBRARY_PATH);
  }

  public Path getSerializeEnvironmentBgpTablesPath() {
    return getOutputPath().resolve(BfConsts.RELPATH_SERIALIZED_ENVIRONMENT_BGP_TABLES);
  }

  public Path getSerializeEnvironmentRoutingTablesPath() {
    return getOutputPath().resolve(BfConsts.RELPATH_SERIALIZED_ENVIRONMENT_ROUTING_TABLES);
  }

  public Path getSerializeVendorPath() {
    return getOutputPath().resolve(BfConsts.RELPATH_VENDOR_SPECIFIC_CONFIG_DIR);
  }

  public Path getValidateSnapshotAnswerPath() {
    return getOutputPath().resolve(BfConsts.RELPATH_VALIDATE_SNAPSHOT_ANSWER);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name);
  }

  public void setBasePath(Path basePath) {
    _basePath = basePath;
  }

  public void setName(SnapshotId name) {
    _name = name;
  }

  public Path getParseEnvironmentBgpTablesAnswerPath() {
    return getOutputPath().resolve(BfConsts.RELPATH_ENVIRONMENT_BGP_TABLES_ANSWER);
  }

  public Path getParseEnvironmentRoutingTablesAnswerPath() {
    return getOutputPath().resolve(BfConsts.RELPATH_ENVIRONMENT_ROUTING_TABLES_ANSWER);
  }
}
