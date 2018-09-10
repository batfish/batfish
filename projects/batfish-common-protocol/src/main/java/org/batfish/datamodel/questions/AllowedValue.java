package org.batfish.datamodel.questions;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AllowedValue {
  private static final String PROP_VALUE = "value";
  private static final String PROP_DESCRIPTION = "description";
  private String _value;
  private String _description;

  @JsonCreator
  private static @Nonnull AllowedValue create(
      @JsonProperty(PROP_VALUE) String value, @JsonProperty(PROP_DESCRIPTION) String description) {
    return new AllowedValue(requireNonNull(value), description);
  }

  public AllowedValue(String value, @Nullable String description) {
    _value = value;
    _description = description;
  }

  @JsonProperty(PROP_DESCRIPTION)
  public String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_VALUE)
  public String getValue() {
    return _value;
  }

  @JsonProperty(PROP_DESCRIPTION)
  public void setDescription(String description) {
    _description = description;
  }

  @JsonProperty(PROP_VALUE)
  public void setValue(String value) {
    _value = value;
  }

  @Override
  public String toString() {
    return _description == null ? _value : String.format("%s: %s", _value, _description);
  }
}
