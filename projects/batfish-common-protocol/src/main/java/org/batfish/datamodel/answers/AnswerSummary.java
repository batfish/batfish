package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

/** A general way to summarize what the answer contains, using three basic integers */
public class AnswerSummary {
  private static final String PROP_NOTES = "notes";
  private static final String PROP_NUM_FAILED = "numFailed";
  private static final String PROP_NUM_PASSED = "numPassed";
  private static final String PROP_NUM_RESULTS = "numResults";

  private String _notes = "";

  private int _numFailed;

  private int _numPassed;

  private int _numResults;

  @JsonCreator
  public AnswerSummary() {}

  public AnswerSummary(String notes, int numFailed, int numPassed, int numResults) {
    _notes = notes;
    _numFailed = numFailed;
    _numPassed = numPassed;
    _numResults = numResults;
  }

  /**
   * Combined the current summary with another summary, by summing up individual fields.
   *
   * @param other The other summary to be combined with.
   */
  public void combine(AnswerSummary other) {
    if (other != null) {
      _notes =
          (Strings.isNullOrEmpty(_notes)) ? other.getNotes() : _notes + "; " + other.getNotes();
      _numFailed += other.getNumFailed();
      _numPassed += other.getNumPassed();
      _numResults += other.getNumResults();
    }
  }

  @JsonProperty(PROP_NOTES)
  public String getNotes() {
    return _notes;
  }

  @JsonProperty(PROP_NUM_FAILED)
  public int getNumFailed() {
    return _numFailed;
  }

  @JsonProperty(PROP_NUM_PASSED)
  public int getNumPassed() {
    return _numPassed;
  }

  @JsonProperty(PROP_NUM_RESULTS)
  public int getNumResults() {
    return _numResults;
  }

  public void reset() {
    _notes = null;
    _numFailed = 0;
    _numPassed = 0;
    _numResults = 0;
  }

  @JsonProperty(PROP_NOTES)
  public void setNotes(String notes) {
    _notes = notes;
  }

  @JsonProperty(PROP_NUM_FAILED)
  public void setNumFailed(int numFailed) {
    _numFailed = numFailed;
  }

  @JsonProperty(PROP_NUM_PASSED)
  public void setNumPassed(int numPassed) {
    _numPassed = numPassed;
  }

  @JsonProperty(PROP_NUM_RESULTS)
  public void setNumResults(int numResults) {
    _numResults = numResults;
  }
}
