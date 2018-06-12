package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class StubSettings implements Serializable {

  public static class Builder {

    private boolean _suppressType3;

    public StubSettings build() {
      return new StubSettings(this);
    }

    public Builder setSuppressType3(boolean suppressType3) {
      _suppressType3 = suppressType3;
      return this;
    }
  }

  private static final String PROP_SUPPRESS_TYPE3 = "suppressType3";

  private static final long serialVersionUID = 1L;

  public static Builder builder() {
    return new Builder();
  }

  private final boolean _suppressType3;

  @JsonCreator
  private StubSettings(@JsonProperty(PROP_SUPPRESS_TYPE3) boolean suppressType3) {
    _suppressType3 = suppressType3;
  }

  public StubSettings(Builder builder) {
    _suppressType3 = builder._suppressType3;
  }

  @JsonProperty(PROP_SUPPRESS_TYPE3)
  public boolean getSuppressType3() {
    return _suppressType3;
  }
}
