package org.batfish.identifiers;

import com.google.common.collect.ImmutableSet;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.apache.commons.io.FileUtils;
import org.batfish.common.util.CommonUtil;
import org.batfish.storage.FileBasedStorageDirectoryProvider;

public class FileBasedIdResolver implements IdResolver {

  private static final String ID_EXTENSION = ".id";
  
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
      AnswerId baseAnswerId, Set<IssueSettingsId> issueSettingsIds) {
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
    return new NetworkId(CommonUtil.readFile(getNetworkIdPath(network)));
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
  public boolean hasAnalysisId(String analysis, NetworkId networkId) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public boolean hasIssueSettingsId(String majorIssueType, NetworkId networkId) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public boolean hasNetworkId(String network) {
    return Files.exists(getNetworkIdPath(network));
  }

  protected @Nonnull Path getNetworkIdPath(String network) {
    return _d.getNetworkIdsDir().resolve(String.format("%s%s", network,ID_EXTENSION));
  }

  @Override
  public boolean hasQuestionId(String question, NetworkId networkId, AnalysisId analysisId) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public boolean hasQuestionSettingsId(String questionClassId, NetworkId networkId) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public Set<String> listAnalyses(NetworkId networkId) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public Set<String> listSnapshots(NetworkId networkId) {
    Path snapshotIdsDir = _d.getSnapshotIdsDir(networkId);
    if (!Files.exists(snapshotIdsDir)) {
      return ImmutableSet.of();
    }
    try (Stream<Path> files = CommonUtil.list((snapshotIdsDir))) {
      return files
          .filter(path -> path.endsWith(ID_EXTENSION))
          .map(Path::getFileName)
          .map(Path::toString)
          .map(
              nameWithExtension ->
                  nameWithExtension.substring(
                      0, nameWithExtension.length() - ID_EXTENSION.length()))
          .collect(ImmutableSet.toImmutableSet());
    }
  }
}
