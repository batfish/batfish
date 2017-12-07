package org.batfish.datamodel.pojo;

import java.util.Map;

public abstract class BfObject {

  private final String _id;

  private Map<String, String> _properties;

  public BfObject(String id) {
    _id = id;
  }

  @Override
  public boolean equals(Object o) {
    return (o != null && o.getClass() == this.getClass() && ((BfObject) o).getId() == _id);
  }

  public String getId() {
    return _id;
  }

  public Map<String, String> getProperties() {
    return _properties;
  }

  @Override
  public int hashCode() {
    return _id.hashCode();
  }

  public void setProperties(Map<String, String> properties) {
    _properties = properties;
  }
}
