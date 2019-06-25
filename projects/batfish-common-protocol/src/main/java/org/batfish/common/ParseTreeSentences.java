package org.batfish.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ParseTreeSentences implements Serializable {
  private static final String PROP_SENTENCES = "sentences";

  protected List<String> _sentences;

  @JsonCreator
  public ParseTreeSentences() {
    _sentences = new ArrayList<>();
  }

  public void appendToLastSentence(String appendStr) {
    if (_sentences.isEmpty()) {
      _sentences.add(appendStr);
    } else {
      String finalStr = _sentences.get(_sentences.size() - 1) + appendStr;
      _sentences.remove(_sentences.size() - 1);
      _sentences.add(finalStr);
    }
  }

  @JsonProperty(PROP_SENTENCES)
  public List<String> getSentences() {
    return _sentences;
  }

  @JsonIgnore
  public boolean isEmpty() {
    return _sentences.isEmpty();
  }

  @JsonProperty(PROP_SENTENCES)
  public void setSentences(List<String> sentences) {
    _sentences = sentences;
  }
}
