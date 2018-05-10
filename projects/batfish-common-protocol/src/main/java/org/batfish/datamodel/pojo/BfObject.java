package org.batfish.datamodel.pojo;

import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

public abstract class BfObject {

  protected static final String PROP_ID = "id";

  @Nullable private final String _id;

  private Map<String, String> _properties;

  public BfObject(String id) {
    _id = id;
  }

  @Override
  public boolean equals(Object o) {
    return (o != null
        && o.getClass() == this.getClass()
        && Objects.equals(_id, ((BfObject) o).getId()));
  }

  public String getId() {
    return _id;
  }

  public Map<String, String> getProperties() {
    return _properties;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_id);
  }

  public void setProperties(Map<String, String> properties) {
    _properties = properties;
  }
}
