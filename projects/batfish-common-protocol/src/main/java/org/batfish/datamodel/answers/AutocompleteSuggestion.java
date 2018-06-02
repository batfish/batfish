package org.batfish.datamodel.answers;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.util.BatfishObjectMapper;

public class AutocompleteSuggestion {

  public enum CompletionType {
    INTERFACE_PROPERTY,
    NODE,
    NODE_PROPERTY
  }

  @Nullable private final String _description;
  private final boolean _isPartial;
  private int _rank;
  @Nonnull private final String _text;

  public AutocompleteSuggestion(String text, boolean isPartial) {
    this(text, isPartial, null, -1);
  }

  public AutocompleteSuggestion(String text, boolean isPartial, String description) {
    this(text, isPartial, description, -1);
  }

  public AutocompleteSuggestion(String text, boolean isPartial, String description, int rank) {
    _text = text;
    _isPartial = isPartial;
    _description = description;
    _rank = rank;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AutocompleteSuggestion)) {
      return false;
    }
    AutocompleteSuggestion os = (AutocompleteSuggestion) o;
    return Objects.equals(_description, os._description)
        && Objects.equals(_isPartial, os._isPartial)
        && Objects.equals(_rank, os._rank)
        && Objects.equals(_text, os._text);
  }

  public String getDescription() {
    return _description;
  }

  public boolean getIsPartial() {
    return _isPartial;
  }

  public int getRank() {
    return _rank;
  }

  public String getText() {
    return _text;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_description, _isPartial, _rank, _text);
  }

  public void setRank(int rank) {
    _rank = rank;
  }

  @Override
  public String toString() {
    try {
      return BatfishObjectMapper.mapper().writeValueAsString(this);
    } catch (JsonProcessingException e) {
      return "Couldn't serialize AutocompleteSuggestion: " + e.getMessage();
    }
  }
}
