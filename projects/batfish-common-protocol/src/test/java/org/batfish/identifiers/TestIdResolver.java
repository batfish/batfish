package org.batfish.identifiers;

import java.util.Optional;
import java.util.Set;

public class TestIdResolver implements IdResolver {

  @Override
  public Optional<AnalysisId> getAnalysisId(String analysis, NetworkId networkId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AnswerId getBaseAnswerId(
      NetworkId networkId,
      SnapshotId snapshotId,
      QuestionId questionId,
      QuestionSettingsId questionSettingsId,
      NodeRolesId networkNodeRolesId,
      SnapshotId referenceSnapshotId,
      AnalysisId analysisId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AnswerId getFinalAnswerId(AnswerId baseAnswerId, Set<IssueSettingsId> issueSettingsIds) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<IssueSettingsId> getIssueSettingsId(String majorIssueType, NetworkId networkId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<NetworkId> getNetworkId(String network) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<NodeRolesId> getNetworkNodeRolesId(NetworkId networkId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<QuestionId> getQuestionId(
      String question, NetworkId networkId, AnalysisId analysisId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<QuestionSettingsId> getQuestionSettingsId(
      String questionClassId, NetworkId networkId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Optional<SnapshotId> getSnapshotId(String snapshot, NetworkId networkId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public NodeRolesId getSnapshotNodeRolesId(NetworkId networkId, SnapshotId snapshotId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasAnalysisId(String analysis, NetworkId networkId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasIssueSettingsId(String majorIssueType, NetworkId networkId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasNetworkId(String network) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasNetworkNodeRolesId(NetworkId networkId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasQuestionId(String question, NetworkId networkId, AnalysisId analysisId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasQuestionSettingsId(String questionClassId, NetworkId networkId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasSnapshotId(String snapshot, NetworkId networkId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> listAnalyses(NetworkId networkId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> listNetworks() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> listQuestions(NetworkId networkId, AnalysisId analysisId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> listSnapshots(NetworkId networkId) {
    throw new UnsupportedOperationException();
  }
}
