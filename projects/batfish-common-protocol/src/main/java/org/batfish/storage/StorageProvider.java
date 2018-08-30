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

  @Nullable
  ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElement(
      @Nonnull String network, @Nonnull String snapshot);

  @Nullable
  Topology loadLegacyTopology(@Nonnull String network, @Nonnull String snapshot);

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

  void storeAnswer(
      @Nonnull String answerStr,
      @Nonnull String network,
      @Nonnull String baseSnapshotName,
      @Nullable String deltaSnapshotName,
      @Nullable String analysisName,
      @Nonnull String questionName);

  void storeAnswerMetadata(
      @Nonnull AnswerMetadata answerMetrics,
      @Nonnull String network,
      @Nullable String analysisName,
      @Nonnull String baseSnapshotName,
      @Nullable String deltaSnapshotName,
      @Nonnull String questionName);

  @Nonnull
  String loadQuestion(
      @Nonnull String network,
      @Nonnull String snapshot,
      @Nullable String analysis,
      @Nonnull String question);
}
