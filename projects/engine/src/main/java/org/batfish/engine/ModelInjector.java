package org.batfish.engine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.identifiers.SnapshotId;
import org.batfish.job.FinalizeShim;
import org.batfish.version.BatfishVersion;

/**
 * Injects self-built vendor-independent models into a snapshot.
 *
 * <p>Models go through the same two passes a parsed config does. Finalization runs here; the
 * load-time post-processing (interface dependencies, L1 topology, OSPF neighbors, blacklists) runs
 * when Batfish loads the snapshot from storage. That is why injection goes through storage and not
 * through the in-memory config cache, which skips post-processing.
 */
public final class ModelInjector {

  /** Why a model was refused. Invalid models are never handed to the data plane. */
  public record Invalid(String node, String reason) {}

  /** The outcome of an injection: either stored, or refused with reasons. */
  public record Result(List<Invalid> invalid, SortedMap<String, Warnings> warnings) {
    public boolean ok() {
      return invalid.isEmpty();
    }
  }

  private final EngineBatfish _engine;

  public ModelInjector(@Nonnull EngineBatfish engine) {
    _engine = engine;
  }

  /**
   * Finalizes every model and, only if all of them are valid, stores them as {@code snapshot}.
   * Nothing is written when any model is invalid.
   */
  public @Nonnull Result inject(
      @Nonnull SnapshotId snapshot,
      @Nonnull Map<String, Configuration> configurations,
      @Nullable Layer1Topology layer1) {
    ImmutableList.Builder<Invalid> invalid = ImmutableList.builder();
    ImmutableSortedMap.Builder<String, Warnings> warnings = ImmutableSortedMap.naturalOrder();
    for (Map.Entry<String, Configuration> e : configurations.entrySet()) {
      Configuration c = e.getValue();
      if (!e.getKey().equals(c.getHostname())) {
        invalid.add(
            new Invalid(e.getKey(), "map key differs from hostname '" + c.getHostname() + "'"));
        continue;
      }
      Warnings w = new Warnings(true, true, true);
      try {
        FinalizeShim.finalizeConfiguration(c, w);
      } catch (RuntimeException ex) {
        invalid.add(new Invalid(e.getKey(), String.valueOf(ex.getMessage())));
        continue;
      }
      warnings.put(e.getKey(), w);
    }
    Result result = new Result(invalid.build(), warnings.build());
    if (!result.ok()) {
      return result;
    }
    ConvertConfigurationAnswerElement ccae = new ConvertConfigurationAnswerElement();
    ccae.setVersion(BatfishVersion.getVersionStatic());
    ccae.getWarnings().putAll(result.warnings());
    try {
      _engine
          .storage()
          .storeConfigurations(configurations, ccae, layer1, EngineBatfish.NETWORK, snapshot);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
    return result;
  }
}
