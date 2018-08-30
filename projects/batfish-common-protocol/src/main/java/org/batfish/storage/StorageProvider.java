package org.batfish.storage;

import java.util.Map;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.AnswerMetadata;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;

public interface StorageProvider {

  /**
   * Returns the compressed configuration files for the given snapshot. If a serialized copy of
   * these configurations is not already present, then this function returns {@code null}.
   */
  @Nullable
  SortedMap<String, Configuration> loadCompressedConfigurations(
      @Nonnull String network, @Nonnull String snapshot);

  /**
   * Returns the configuration files for the given snapshot. If a serialized copy of these
   * configurations is not already present, then this function returns {@code null}.
   */
  @Nullable
  SortedMap<String, Configuration> loadConfigurations(
      @Nonnull String network, @Nonnull String snapshot);

  /**
   * Returns the {@link ConvertConfigurationAnswerElement} that is the result of the phase that
   * converts vendor-specific configurations to vendor-independent configurations.
   *
   * @param network The name of the network
   * @param snapshot Then name of the snapshot
   */
  @Nullable
  ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElement(
      @Nonnull String network, @Nonnull String snapshot);

  /**
   * Returns the old-style combined layer-1 through layer-3 topology provided in the given snapshot
   *
   * @param network The name of the network
   * @param snapshot The name of the snapshot
   */
  @Nullable
  Topology loadLegacyTopology(@Nonnull String network, @Nonnull String snapshot);

  /**
   * Returns the layer-1 topology of the network provided in the given snapshot
   *
   * @param network The name of the network
   * @param snapshot The name of the snapshot
   */
  @Nullable
  Layer1Topology loadLayer1Topology(@Nonnull String network, @Nonnull String snapshot);

  /**
   * Stores the configurations into the compressed config path for the given snapshot. Will replace
   * any previously-stored compressed configurations.
   */
  void storeCompressedConfigurations(
      @Nonnull Map<String, Configuration> configurations,
      @Nonnull String network,
      @Nonnull String snapshot);

  /**
   * Stores the configuration information into the given snapshot. Will replace any
   * previously-stored configurations.
   */
  void storeConfigurations(
      @Nonnull Map<String, Configuration> configurations,
      @Nonnull ConvertConfigurationAnswerElement convertAnswerElement,
      @Nonnull String network,
      @Nonnull String snapshot);

  /**
   * Store the answer to an ad-hoc or analysis question.
   *
   * @param answerStr The text of the answer
   * @param network The name of the network
   * @param baseSnapshot The name of the base snapshot
   * @param deltaSnapshot (optional) The name of the deltaSnapshot for a differential question, or
   *     {@code null} for a non-differential question
   * @param analysis (optional) The name of the analysis for an analysis question, or {@code null}
   *     for an ad-hoc question
   * @param question The name of the question
   */
  void storeAnswer(
      @Nonnull String answerStr,
      @Nonnull String network,
      @Nonnull String baseSnapshot,
      @Nullable String deltaSnapshot,
      @Nullable String analysis,
      @Nonnull String question);

  /**
   * Store the metadata for the answer to an ad-hoc or analysis question.
   *
   * @param answerStr The text of the answer
   * @param network The name of the network
   * @param baseSnapshot The name of the base snapshot
   * @param deltaSnapshot (optional) The name of the deltaSnapshot for a differential question, or
   *     {@code null} for a non-differential question
   * @param analysis (optional) The name of the analysis for an analysis question, or {@code null}
   *     for an ad-hoc question
   * @param question The name of the question
   */
  void storeAnswerMetadata(
      @Nonnull AnswerMetadata answerMetrics,
      @Nonnull String network,
      @Nullable String analysis,
      @Nonnull String baseSnapshot,
      @Nullable String deltaSnapshot,
      @Nonnull String question);

  /**
   * Load the text of a JSON-serialized ad-hoc or analysis question
   *
   * @param network The name of the network
   * @param analysis (optional) The name of the analysis for an analysis question, or {@code null}
   *     for an ad-hoc question
   * @param question The name of the question
   * @return
   */
  @Nonnull
  String loadQuestion(@Nonnull String network, @Nullable String analysis, @Nonnull String question);
}
