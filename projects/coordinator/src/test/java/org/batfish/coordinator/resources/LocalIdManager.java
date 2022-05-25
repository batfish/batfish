package org.batfish.coordinator.resources;

import static java.util.Optional.ofNullable;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.coordinator.id.IdManager;
import org.batfish.identifiers.AnswerId;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.NodeRolesId;
import org.batfish.identifiers.QuestionId;
import org.batfish.identifiers.SnapshotId;

/**
 * Memory-based {@link IdManager} suitable for testing. Compatible with any {@link
 * org.batfish.storage.StorageProvider}.
 */
@ParametersAreNonnullByDefault
public class LocalIdManager implements IdManager {

  private static @Nonnull String hash(String input) {
    return Hashing.murmur3_128().hashString(input, StandardCharsets.UTF_8).toString();
  }

  private static @Nonnull String uuid() {
    return UUID.randomUUID().toString();
  }

  private final Map<String, NetworkId> _networkIds;
  private final Map<NetworkId, NodeRolesId> _networkNodeRolesIds;
  private final Map<NetworkId, Map<String, QuestionId>> _questionIds;
  private final Map<NetworkId, Map<String, SnapshotId>> _snapshotIds;

  public LocalIdManager() {
    _networkIds = new HashMap<>();
    _networkNodeRolesIds = new HashMap<>();
    _questionIds = new HashMap<>();
    _snapshotIds = new HashMap<>();
  }

  @Override
  public void assignNetwork(String network, NetworkId networkId) {
    _networkIds.put(network, networkId);
  }

  @Override
  public void assignNetworkNodeRolesId(NetworkId networkId, NodeRolesId networkNodeRolesId) {
    if (!_networkIds.containsValue(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    _networkNodeRolesIds.put(networkId, networkNodeRolesId);
  }

  @Override
  public void assignQuestion(String question, NetworkId networkId, QuestionId questionId) {
    if (!_networkIds.containsValue(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    _questionIds.computeIfAbsent(networkId, n -> new HashMap<>()).put(question, questionId);
  }

  @Override
  public void assignSnapshot(String snapshot, NetworkId networkId, SnapshotId snapshotId) {
    _snapshotIds.computeIfAbsent(networkId, n -> new HashMap<>()).put(snapshot, snapshotId);
  }

  @Override
  public boolean deleteNetwork(String network) {
    return _networkIds.remove(network) != null;
  }

  @Override
  public boolean deleteQuestion(String question, NetworkId networkId) {
    if (!_networkIds.containsValue(networkId)) {
      return false;
    }
    return _questionIds.computeIfAbsent(networkId, n -> new HashMap<>()).remove(question) != null;
  }

  @Override
  public boolean deleteSnapshot(String snapshot, NetworkId networkId) {
    if (!_networkIds.containsValue(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    return _snapshotIds.computeIfAbsent(networkId, n -> new HashMap<>()).remove(snapshot) != null;
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
  public @Nonnull SnapshotId generateSnapshotId() {
    return new SnapshotId(uuid());
  }

  @Override
  public AnswerId getAnswerId(
      NetworkId networkId,
      SnapshotId snapshotId,
      QuestionId questionId,
      NodeRolesId networkNodeRolesId,
      SnapshotId referenceSnapshotId) {
    return new AnswerId(
        hash(
            ImmutableList.of(
                    networkId,
                    snapshotId,
                    questionId,
                    networkNodeRolesId,
                    ofNullable(referenceSnapshotId))
                .toString()));
  }

  @Override
  public Optional<NetworkId> getNetworkId(String network) {
    return ofNullable(_networkIds.get(network));
  }

  @Override
  public Optional<NodeRolesId> getNetworkNodeRolesId(NetworkId networkId) {
    if (!_networkIds.containsValue(networkId)) {
      return Optional.empty();
    }
    return ofNullable(_networkNodeRolesIds.get(networkId));
  }

  @Override
  public Optional<QuestionId> getQuestionId(String question, NetworkId networkId) {
    if (!_networkIds.containsValue(networkId)) {
      return Optional.empty();
    }
    return ofNullable(_questionIds.computeIfAbsent(networkId, n -> new HashMap<>()).get(question));
  }

  @Override
  public Optional<SnapshotId> getSnapshotId(String snapshot, NetworkId networkId) {
    if (!_networkIds.containsValue(networkId)) {
      return Optional.empty();
    }
    return ofNullable(_snapshotIds.computeIfAbsent(networkId, n -> new HashMap<>()).get(snapshot));
  }

  @Override
  public NodeRolesId getSnapshotNodeRolesId(NetworkId networkId, SnapshotId snapshotId) {
    return new NodeRolesId(hash(ImmutableList.of(networkId, snapshotId).toString()));
  }

  @Override
  public boolean hasNetworkId(String network) {
    return _networkIds.containsKey(network);
  }

  @Override
  public boolean hasNetworkNodeRolesId(NetworkId networkId) {
    if (!_networkIds.containsValue(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    return _networkNodeRolesIds.containsKey(networkId);
  }

  @Override
  public boolean hasQuestionId(String question, NetworkId networkId) {
    if (!_networkIds.containsValue(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    return _questionIds.computeIfAbsent(networkId, n -> new HashMap<>()).containsKey(question);
  }

  @Override
  public boolean hasSnapshotId(String snapshot, NetworkId networkId) {
    if (!_networkIds.containsValue(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    return _snapshotIds.computeIfAbsent(networkId, n -> new HashMap<>()).containsKey(snapshot);
  }

  @Override
  public Set<String> listNetworks() {
    return _networkIds.keySet();
  }

  @Override
  public Set<String> listQuestions(NetworkId networkId) {
    if (!_networkIds.containsValue(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    return _questionIds.computeIfAbsent(networkId, n -> new HashMap<>()).keySet();
  }

  @Override
  public Set<String> listSnapshots(NetworkId networkId) {
    if (!_networkIds.containsValue(networkId)) {
      throw new IllegalArgumentException(String.format("No network with ID: '%s'", networkId));
    }
    return _snapshotIds.computeIfAbsent(networkId, n -> new HashMap<>()).keySet();
  }
}
