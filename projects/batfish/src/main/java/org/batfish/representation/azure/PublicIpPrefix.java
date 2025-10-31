package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class PublicIpPrefix extends Resource implements Serializable {

  private final Properties _properties;

  @JsonCreator
  public PublicIpPrefix(
      @JsonProperty(AzureEntities.JSON_KEY_ID) String id,
      @JsonProperty(AzureEntities.JSON_KEY_NAME) String name,
      @JsonProperty(AzureEntities.JSON_KEY_TYPE) String type,
      @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) Properties properties) {
    super(name, id, type);
    _properties = properties;
  }

  public Properties getProperties() {
    return _properties;
  }

  public static class Properties implements Serializable {}
}
