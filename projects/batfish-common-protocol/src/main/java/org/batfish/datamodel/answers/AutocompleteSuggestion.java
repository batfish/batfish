package org.batfish.datamodel.answers;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.util.BatfishObjectMapper;

public class AutocompleteSuggestion {

  public enum CompletionType {
    BGP_PEER_PROPERTY,
    BGP_PROCESS_PROPERTY,
    INTERFACE_PROPERTY,
    NAMED_STRUCTURE,
    NODE,
    NODE_PROPERTY,
    OSPF_PROPERTY
  }

  public static final int DEFAULT_RANK = Integer.MAX_VALUE;

  @Nullable private final String _description;
  private final boolean _isPartial;
  private int _rank;
  @Nonnull private final String _text;

  public AutocompleteSuggestion(String text, boolean isPartial) {
    this(text, isPartial, null, DEFAULT_RANK);
  }

  public AutocompleteSuggestion(String text, boolean isPartial, String description) {
    this(text, isPartial, description, DEFAULT_RANK);
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
    // ignore rank and description
    return Objects.equals(_isPartial, ((AutocompleteSuggestion) o)._isPartial)
        && Objects.equals(_text, ((AutocompleteSuggestion) o)._text);
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
    // ignore rank and description
    return Objects.hash(_isPartial, _text);
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
