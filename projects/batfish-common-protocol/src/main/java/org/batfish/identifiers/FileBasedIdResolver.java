package org.batfish.identifiers;

import java.nio.file.Path;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.storage.FileBasedStorageDirectoryProvider;

public class FileBasedIdResolver implements IdResolver {

  protected final FileBasedStorageDirectoryProvider _d;

  public FileBasedIdResolver(Path storageBase) {
    _d = new FileBasedStorageDirectoryProvider(storageBase);
  }

  @Override
  public @Nonnull AnalysisId getAnalysisId(String analysis, NetworkId networkId) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public @Nonnull AnswerId getBaseAnswerId(
      NetworkId networkId,
      SnapshotId snapshotId,
      QuestionId questionId,
      QuestionSettingsId questionSettingsId,
      SnapshotId referenceSnapshotId,
      AnalysisId analysisId) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public @Nonnull AnswerId getFinalAnswerId(
      AnswerId baseAnswerId,
      Set<IssueSettingsId> issueSettingsIds) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public @Nonnull IssueSettingsId getIssueSettingsId(String majorIssueType, NetworkId networkId) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public @Nonnull NetworkId getNetworkId(String network) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public @Nonnull QuestionId getQuestionId(
      String question, NetworkId networkId, AnalysisId analysisId) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public @Nonnull QuestionSettingsId getQuestionSettingsId(
      String questionClassId, NetworkId networkId) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public @Nonnull SnapshotId getSnapshotId(String snapshot, NetworkId networkId) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public @Nonnull boolean hasIssueSettingsId(String majorIssueType, NetworkId networkId) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public @Nonnull boolean hasQuestionSettingsId(String questionClassId, NetworkId networkId) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }
}
