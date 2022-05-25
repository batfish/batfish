package org.batfish.identifiers;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.storage.StorageProvider;

/**
 * Storage-based {@link IdResolver} that reads IDs via {@link org.batfish.storage.StorageProvider}.
 */
public class StorageBasedIdResolver implements IdResolver {

  protected static final String NETWORK_NODE_ROLES = "network_node_roles";

  private static @Nonnull String hash(String input) {
    return Hashing.murmur3_128().hashString(input, UTF_8).toString();
  }

  private @Nonnull Set<String> listResolvableNames(Class<? extends Id> idType, Id... ancestors) {
    try {
      return _s.listResolvableNames(idType, ancestors);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private @Nonnull Optional<String> readId(
      Class<? extends Id> idType, String name, Id... ancestors) {
    try {
      return _s.readId(idType, name, ancestors);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  protected final StorageProvider _s;

  public StorageBasedIdResolver(StorageProvider s) {
    _s = s;
  }

  @Override
  public @Nonnull AnswerId getAnswerId(
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
  public @Nonnull Optional<NetworkId> getNetworkId(String network) {
    return readId(NetworkId.class, network).map(NetworkId::new);
  }

  @Override
  public Optional<NodeRolesId> getNetworkNodeRolesId(NetworkId networkId) {
    return readId(NodeRolesId.class, NETWORK_NODE_ROLES, networkId).map(NodeRolesId::new);
  }

  @Override
  public @Nonnull Optional<QuestionId> getQuestionId(String question, NetworkId networkId) {
    Id[] ancestors = new Id[] {networkId};
    return readId(QuestionId.class, question, ancestors).map(QuestionId::new);
  }

  @Override
  public @Nonnull Optional<SnapshotId> getSnapshotId(String snapshot, NetworkId networkId) {
    return readId(SnapshotId.class, snapshot, networkId).map(SnapshotId::new);
  }

  @Override
  public NodeRolesId getSnapshotNodeRolesId(NetworkId networkId, SnapshotId snapshotId) {
    return new NodeRolesId(hash(ImmutableList.of(networkId, snapshotId).toString()));
  }

  @Override
  public boolean hasNetworkId(String network) {
    return _s.hasId(NetworkId.class, network);
  }

  @Override
  public boolean hasNetworkNodeRolesId(NetworkId networkId) {
    return _s.hasId(NodeRolesId.class, NETWORK_NODE_ROLES, networkId);
  }

  @Override
  public boolean hasQuestionId(String question, NetworkId networkId) {
    Id[] ancestors = new Id[] {networkId};
    return _s.hasId(QuestionId.class, question, ancestors);
  }

  @Override
  public boolean hasSnapshotId(String snapshot, NetworkId networkId) {
    return _s.hasId(SnapshotId.class, snapshot, networkId);
  }

  @Override
  public @Nonnull Set<String> listNetworks() {
    return listResolvableNames(NetworkId.class);
  }

  @Override
  public @Nonnull Set<String> listQuestions(NetworkId networkId) {
    Id[] ancestors = new Id[] {networkId};
    return listResolvableNames(QuestionId.class, ancestors);
  }

  @Override
  public @Nonnull Set<String> listSnapshots(NetworkId networkId) {
    return listResolvableNames(SnapshotId.class, networkId);
  }
}
