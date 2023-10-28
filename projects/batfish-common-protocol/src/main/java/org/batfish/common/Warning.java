package org.batfish.common;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class Warning implements Serializable, Comparable<Warning> {

  private static final String PROP_TAG = "tag";
  private static final String PROP_TEXT = "text";

  private final @Nonnull String _text;
  private final @Nullable String _tag;

  public Warning(String text, @Nullable String tag) {
    _text = text;
    _tag = tag;
  }

  @JsonProperty(PROP_TAG)
  public @Nullable String getTag() {
    return _tag;
  }

  @JsonProperty(PROP_TEXT)
  public @Nonnull String getText() {
    return _text;
  }

  @Override
  public int compareTo(Warning o) {
    return Comparator.comparing(Warning::getText)
        .thenComparing(Warning::getTag, Comparator.nullsFirst(String::compareTo))
        .compare(this, o);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Warning)) {
      return false;
    }
    Warning w = (Warning) o;
    return _text.equals(w._text) && Objects.equals(_tag, w._tag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_text, _tag);
  }

  @JsonCreator
  private static Warning jsonCreator(
      @JsonProperty(PROP_TEXT) @Nullable String text,
      @JsonProperty(PROP_TAG) @Nullable String tag) {
    checkArgument(text != null, "Missing %s", PROP_TEXT);
    return new Warning(text, tag);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("_text", _text).add("_tag", _tag).toString();
  }
}
