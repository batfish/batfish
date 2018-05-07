package org.batfish.datamodel.answers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AutocompleteSuggestion {
  @Nullable private final String _description;
  private final boolean _isPartial;
  @Nonnull private final String _text;

  public AutocompleteSuggestion(String text, boolean isPartial) {
    this(text, isPartial, null);
  }

  public AutocompleteSuggestion(String text, boolean isPartial, String description) {
    _text = text;
    _isPartial = isPartial;
    _description = description;
  }

  public String getDescription() {
    return _description;
  }

  public boolean getIsPartial() {
    return _isPartial;
  }

  public String getText() {
    return _text;
  }
}
