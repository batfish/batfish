package org.batfish.storage;

import java.util.Map;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.AnswerMetadata;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;

/** Storage backend for loading and storing persistent data used by Batfish */
@ParametersAreNonnullByDefault
public interface StorageProvider {

  /**
   * Returns the compressed configuration files for the given snapshot. If a serialized copy of
   * these configurations is not already present, then this function returns {@code null}.
   */
  @Nullable
  SortedMap<String, Configuration> loadCompressedConfigurations(String network, String snapshot);

  /**
   * Returns the configuration files for the given snapshot. If a serialized copy of these
   * configurations is not already present, then this function returns {@code null}.
   */
  @Nullable
  SortedMap<String, Configuration> loadConfigurations(String network, String snapshot);

  /**
   * Returns the {@link ConvertConfigurationAnswerElement} that is the result of the phase that
   * converts vendor-specific configurations to vendor-independent configurations.
   *
   * @param network The name of the network
   * @param snapshot Then name of the snapshot
   */
  @Nullable
  ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElement(
      String network, String snapshot);

  /**
   * Returns the old-style combined layer-1 through layer-3 topology provided in the given snapshot
   *
   * @param network The name of the network
   * @param snapshot The name of the snapshot
   */
  @Nullable
  Topology loadLegacyTopology(String network, String snapshot);

  /**
   * Returns the layer-1 topology of the network provided in the given snapshot
   *
   * @param network The name of the network
   * @param snapshot The name of the snapshot
   */
  @Nullable
  Layer1Topology loadLayer1Topology(String network, String snapshot);

  /**
   * Stores the configurations into the compressed config path for the given snapshot. Will replace
   * any previously-stored compressed configurations.
   */
  void storeCompressedConfigurations(
      Map<String, Configuration> configurations, String network, String snapshot);

  /**
   * Stores the configuration information into the given snapshot. Will replace any
   * previously-stored configurations.
   */
  void storeConfigurations(
      Map<String, Configuration> configurations,
      ConvertConfigurationAnswerElement convertAnswerElement,
      String network,
      String snapshot);

  /**
   * Store the answer to an ad-hoc or analysis question.
   *
   * @param answerStr The text of the answer
   * @param network The name of the network
   * @param snapshot The name of the base snapshot
   * @param question The name of the question
   * @param referenceSnapshot (optional) The name of the deltaSnapshot for a differential question, or
   *     {@code null} for a non-differential question
   * @param analysis (optional) The name of the analysis for an analysis question, or {@code null}
   *     for an ad-hoc question
   */
  void storeAnswer(
      String answerStr,
      String network,
      String snapshot,
      String question,
      @Nullable String referenceSnapshot,
      @Nullable String analysis);

  /**
   * Store the metadata for the answer to an ad-hoc or analysis question.
   *
   * @param answerMetrics The metadata to store
   * @param network The name of the network
   * @param snapshot The name of the base snapshot
   * @param question The name of the question
   * @param referenceSnapshot (optional) The name of the deltaSnapshot for a differential question,
   *     or {@code null} for a non-differential question
   * @param analysis (optional) The name of the analysis for an analysis question, or {@code null}
   *     for an ad-hoc question
   */
  void storeAnswerMetadata(
      AnswerMetadata answerMetrics,
      String network,
      String snapshot,
      String question,
      @Nullable String referenceSnapshot,
      @Nullable String analysis);

  /**
   * Load the text of a JSON-serialized ad-hoc or analysis question
   *
   * @param network The name of the network
   * @param question The name of the question
   * @param analysis (optional) The name of the analysis for an analysis question, or {@code null}
   *     for an ad-hoc question
   */
  @Nonnull
  String loadQuestion(String network, String question, @Nullable String analysis);
}
