package org.batfish.client.answer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.datamodel.answers.AnswerElement;

public class LoadQuestionAnswerElement extends AnswerElement {
  private static final String PROP_ADDED = "added";
  private static final String PROP_NUM_LOADED = "numLoaded";
  private static final String PROP_REPLACED = "replaced";

  private SortedSet<String> _added;

  private int _numLoaded;

  private SortedSet<String> _replaced;

  @JsonCreator
  public LoadQuestionAnswerElement() {
    _added = new TreeSet<>();
    _replaced = new TreeSet<>();
  }

  @JsonProperty(PROP_ADDED)
  public SortedSet<String> getAdded() {
    return _added;
  }

  @JsonProperty(PROP_NUM_LOADED)
  public int getNumLoaded() {
    return _numLoaded;
  }

  @JsonProperty(PROP_REPLACED)
  public SortedSet<String> getReplaced() {
    return _replaced;
  }

  @JsonProperty(PROP_ADDED)
  public void setAdded(SortedSet<String> added) {
    _added = added;
  }

  @JsonProperty(PROP_NUM_LOADED)
  public void setNumLoaded(int numLoaded) {
    _numLoaded = numLoaded;
  }

  @JsonProperty(PROP_REPLACED)
  public void setReplaced(SortedSet<String> replaced) {
    _replaced = replaced;
  }
}
