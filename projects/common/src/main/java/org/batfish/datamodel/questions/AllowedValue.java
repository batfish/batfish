package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AllowedValue {
  private static final String PROP_NAME = "name";
  private static final String PROP_DESCRIPTION = "description";
  private String _name;
  private String _description;

  @JsonCreator
  private static @Nonnull AllowedValue create(
      @JsonProperty(PROP_NAME) String name, @JsonProperty(PROP_DESCRIPTION) String description) {
    checkArgument(name != null, String.format("%s is required", PROP_NAME));
    return new AllowedValue(name, description);
  }

  public AllowedValue(String name, @Nullable String description) {
    _name = name;
    _description = description;
  }

  @JsonProperty(PROP_DESCRIPTION)
  public String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_DESCRIPTION)
  public void setDescription(String description) {
    _description = description;
  }

  @JsonProperty(PROP_NAME)
  public void setName(String name) {
    _name = name;
  }

  @Override
  public String toString() {
    return _description == null ? _name : String.format("%s: %s", _name, _description);
  }
}
