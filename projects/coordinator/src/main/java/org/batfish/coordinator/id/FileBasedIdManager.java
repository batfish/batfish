package org.batfish.coordinator.id;

import java.nio.file.Path;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.CommonUtil;
import org.batfish.identifiers.AnalysisId;
import org.batfish.identifiers.FileBasedIdResolver;
import org.batfish.identifiers.IssueSettingsId;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.NodeRolesId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.QuestionSettingsId;
import org.batfish.identifiers.SnapshotId;

/**
 * Filesystem based {@link IdManager} capable of writing mappings used by {@link
 * FileBasedIdResolver}, from which it inherits. Intended to be used together with {@link
 * org.batfish.storage.FileBasedStorage}.
 */
@ParametersAreNonnullByDefault
public class FileBasedIdManager extends FileBasedIdResolver implements IdManager {

  private static @Nonnull String uuid() {
    return UUID.randomUUID().toString();
  }

  public FileBasedIdManager(Path baseDir) {
    super(baseDir);
  }

  @Override
  public void assignAnalysis(String analysis, NetworkId networkId, AnalysisId analysisId) {
    Path idFile = getAnalysisIdPath(analysis, networkId);
    idFile.getParent().toFile().mkdirs();
    CommonUtil.writeFile(idFile, analysisId.getId());
  }

  @Override
  public void assignIssueSettingsId(
      String majorIssueType, NetworkId networkId, IssueSettingsId issueSettingsId) {
    Path idFile = getIssueSettingsIdPath(majorIssueType, networkId);
    idFile.getParent().toFile().mkdirs();
    CommonUtil.writeFile(idFile, issueSettingsId.getId());
  }

  @Override
  public void assignNetwork(String network, NetworkId networkId) {
    Path idFile = getNetworkIdPath(network);
    idFile.getParent().toFile().mkdirs();
    CommonUtil.writeFile(idFile, networkId.getId());
  }

  @Override
  public void assignNetworkNodeRolesId(NetworkId networkId, NodeRolesId networkNodeRolesId) {
    Path idFile = getNetworkNodeRolesIdPath(networkId);
    idFile.getParent().toFile().mkdirs();
    CommonUtil.writeFile(idFile, networkNodeRolesId.getId());
  }

  @Override
  public void assignQuestion(
      String question, NetworkId networkId, QuestionId questionId, AnalysisId analysisId) {
    Path idFile = getQuestionIdPath(question, networkId, analysisId);
    idFile.getParent().toFile().mkdirs();
    CommonUtil.writeFile(idFile, questionId.getId());
  }

  @Override
  public void assignQuestionSettingsId(
      String questionClassId, NetworkId networkId, QuestionSettingsId questionSettingsId) {
    Path idFile = getQuestionSettingsIdPath(questionClassId, networkId);
    idFile.getParent().toFile().mkdirs();
    CommonUtil.writeFile(idFile, questionSettingsId.getId());
  }

  @Override
  public void assignSnapshot(String snapshot, NetworkId networkId, SnapshotId snapshotId) {
    Path idFile = getSnapshotIdPath(snapshot, networkId);
    idFile.getParent().toFile().mkdirs();
    CommonUtil.writeFile(idFile, snapshotId.getId());
  }

  @Override
  public void deleteAnalysis(String analysis, NetworkId networkId) {
    CommonUtil.delete(getAnalysisIdPath(analysis, networkId));
  }

  @Override
  public void deleteNetwork(String network) {
    CommonUtil.delete(getNetworkIdPath(network));
  }

  @Override
  public void deleteQuestion(
      String question, NetworkId networkId, @Nullable AnalysisId analysisId) {
    CommonUtil.delete(getQuestionIdPath(question, networkId, analysisId));
  }

  @Override
  public void deleteSnapshot(String snapshot, NetworkId networkId) {
    CommonUtil.delete(getSnapshotIdPath(snapshot, networkId));
  }

  @Override
  public @Nonnull AnalysisId generateAnalysisId() {
    return new AnalysisId(uuid());
  }

  @Override
  public @Nonnull IssueSettingsId generateIssueSettingsId() {
    return new IssueSettingsId(uuid());
  }

  @Override
  public @Nonnull NetworkId generateNetworkId() {
    return new NetworkId(uuid());
  }

  @Override
  public NodeRolesId generateNetworkNodeRolesId() {
    return new NodeRolesId(uuid());
  }

  @Override
  public @Nonnull QuestionId generateQuestionId() {
    return new QuestionId(uuid());
  }

  @Override
  public @Nonnull QuestionSettingsId generateQuestionSettingsId() {
    return new QuestionSettingsId(uuid());
  }

  @Override
  public @Nonnull SnapshotId generateSnapshotId() {
    return new SnapshotId(uuid());
  }
}
