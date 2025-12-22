package org.batfish.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ParseTreeSentences implements Serializable {
  private static final String PROP_SENTENCES = "sentences";

  // Use StringBuilder internally to avoid quadratic string concatenation overhead
  protected List<StringBuilder> _sentenceBuilders;

  @JsonCreator
  public ParseTreeSentences() {
    _sentenceBuilders = new ArrayList<>();
  }

  public void appendToLastSentence(String appendStr) {
    if (_sentenceBuilders.isEmpty()) {
      _sentenceBuilders.add(new StringBuilder(appendStr));
    } else {
      _sentenceBuilders.get(_sentenceBuilders.size() - 1).append(appendStr);
    }
  }

  /** Add a new sentence to the list. */
  public void addSentence(String sentence) {
    _sentenceBuilders.add(new StringBuilder(sentence));
  }

  /** Add all sentences from another ParseTreeSentences object. */
  public void addAllSentences(ParseTreeSentences other) {
    other._sentenceBuilders.forEach(sb -> _sentenceBuilders.add(new StringBuilder(sb)));
  }

  /**
   * Get an immutable copy of the sentences. Modifications to the returned list will fail. Use
   * {@link #addSentence(String)} or {@link #addAllSentences(ParseTreeSentences)} to add sentences.
   */
  @JsonProperty(PROP_SENTENCES)
  public List<String> getSentences() {
    return _sentenceBuilders.stream()
        .map(StringBuilder::toString)
        .collect(ImmutableList.toImmutableList());
  }

  @JsonIgnore
  public boolean isEmpty() {
    return _sentenceBuilders.isEmpty();
  }

  @JsonProperty(PROP_SENTENCES)
  public void setSentences(List<String> sentences) {
    _sentenceBuilders = sentences.stream().map(StringBuilder::new).collect(Collectors.toList());
  }
}
