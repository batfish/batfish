package org.batfish.identifiers;

import java.util.Optional;
import java.util.Set;

public class TestIdResolver implements IdResolver {

  @Override
  public AnswerId getAnswerId(
      NetworkId networkId,
      SnapshotId snapshotId,
      QuestionId questionId,
      NodeRolesId networkNodeRolesId,
      SnapshotId referenceSnapshotId) {
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
  public Optional<QuestionId> getQuestionId(String question, NetworkId networkId) {
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
  public boolean hasNetworkId(String network) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasNetworkNodeRolesId(NetworkId networkId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasQuestionId(String question, NetworkId networkId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasSnapshotId(String snapshot, NetworkId networkId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> listNetworks() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> listQuestions(NetworkId networkId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> listSnapshots(NetworkId networkId) {
    throw new UnsupportedOperationException();
  }
}
