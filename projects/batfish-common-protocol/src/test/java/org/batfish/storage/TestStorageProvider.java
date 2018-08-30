package org.batfish.storage;

import java.util.Map;
import java.util.SortedMap;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.AnswerMetadata;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;

public class TestStorageProvider implements StorageProvider {

  @Override
  public SortedMap<String, Configuration> loadCompressedConfigurations(
      String network, String snapshot) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public SortedMap<String, Configuration> loadConfigurations(String network, String snapshot) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public ConvertConfigurationAnswerElement loadConvertConfigurationAnswerElement(
      String network, String snapshot) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public Topology loadLegacyTopology(String network, String snapshot) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public Layer1Topology loadLayer1Topology(String network, String snapshot) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void storeCompressedConfigurations(
      Map<String, Configuration> configurations, String network, String snapshot) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void storeConfigurations(
      Map<String, Configuration> configurations,
      ConvertConfigurationAnswerElement convertAnswerElement,
      String network,
      String snapshot) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void storeAnswer(
      String answerStr,
      String network,
      String baseSnapshotName,
      String deltaSnapshotName,
      String analysisName,
      String questionName) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public void storeAnswerMetadata(
      AnswerMetadata answerMetrics,
      String network,
      String analysisName,
      String baseSnapshotName,
      String deltaSnapshotName,
      String questionName) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public String loadQuestion(String network, String snapshot, String analysis, String question) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }
}
