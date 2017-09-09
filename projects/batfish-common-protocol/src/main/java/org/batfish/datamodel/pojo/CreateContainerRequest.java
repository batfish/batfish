package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateContainerRequest {
  private static final String PROP_NAME = "name";
  private static final String PROP_SET_NAME = "setName";

  private final String _name;
  private boolean _setName;

  @JsonCreator
  public CreateContainerRequest(
      @JsonProperty(PROP_NAME) String name, @JsonProperty(PROP_SET_NAME) boolean setName) {
    this._name = name;
    this._setName = setName;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_SET_NAME)
  public boolean getSetName() {
    return _setName;
  }

}
