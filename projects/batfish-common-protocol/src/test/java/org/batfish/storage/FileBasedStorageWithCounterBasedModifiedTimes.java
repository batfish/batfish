package org.batfish.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import org.batfish.common.BatfishLogger;

public class FileBasedStorageWithCounterBasedModifiedTimes extends FileBasedStorage {

  private Instant _questionSettingsTime;
  private Instant _questionTime;
  private Instant _answerTime;

  private long _counter;

  public FileBasedStorageWithCounterBasedModifiedTimes(Path baseDir, BatfishLogger logger) {
    super(baseDir, logger);
    _answerTime = Instant.MIN;
    _questionTime = Instant.MIN;
    _questionSettingsTime = Instant.MIN;
  }

  @Override
  public void storeAnswer(
      String answerStr,
      String network,
      String snapshot,
      String question,
      String referenceSnapshot,
      String analysis) {
    super.storeAnswer(answerStr, network, snapshot, question, referenceSnapshot, analysis);
    _answerTime = newTime();
  }

  @Override
  public void storeQuestion(String questionStr, String network, String question, String analysis) {
    super.storeQuestion(questionStr, network, question, analysis);
    _questionTime = newTime();
  }

  @Override
  public void storeQuestionSettings(String settings, String network, String questionName)
      throws IOException {
    super.storeQuestionSettings(settings, network, questionName);
    _questionSettingsTime = newTime();
  }

  private synchronized Instant newTime() {
    return Instant.ofEpochSecond(++_counter);
  }

  @Override
  public Instant getAnswerLastModifiedTime(
      String network, String snapshot, String question, String referenceSnapshot, String analysis) {
    return _answerTime;
  }

  @Override
  public Instant getQuestionLastModifiedTime(String network, String question, String analysis) {
    return _questionTime;
  }

  @Override
  public Instant getQuestionSettingsLastModifiedTime(
      String network, String question, String analysis) {
    return _questionSettingsTime;
  }
}
