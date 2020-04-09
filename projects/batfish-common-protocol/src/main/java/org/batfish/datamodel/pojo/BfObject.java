package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

public abstract class BfObject {

  protected static final String PROP_ID = "id";

  @Nullable private final String _id;

  private Map<String, String> _properties;

  public BfObject(@Nullable String id) {
    _id = id;
  }

  @Override
  public boolean equals(Object o) {
    return (o != null && o.getClass() == getClass() && Objects.equals(_id, ((BfObject) o).getId()));
  }

  @JsonProperty(PROP_ID)
  @Nullable
  public String getId() {
    return _id;
  }

  public Map<String, String> getProperties() {
    return _properties;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_id);
  }

  public void setProperties(Map<String, String> properties) {
    _properties = properties;
  }
}
