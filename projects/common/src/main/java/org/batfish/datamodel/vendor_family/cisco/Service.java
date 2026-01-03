package org.batfish.datamodel.vendor_family.cisco;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Service implements Serializable {
  private static final String PROP_ENABLED = "enabled";
  private static final String PROP_SUBSERVICES = "subservices";

  private @Nullable Boolean _enabled;

  private @Nonnull SortedMap<String, Service> _subservices;

  @JsonCreator
  public Service() {
    _subservices = new TreeMap<>();
  }

  public void disable() {
    for (Service s : _subservices.values()) {
      s.disable();
    }
    _enabled = false;
  }

  @JsonProperty(PROP_ENABLED)
  public @Nullable Boolean getEnabled() {
    return _enabled;
  }

  @JsonProperty(PROP_SUBSERVICES)
  public @Nonnull SortedMap<String, Service> getSubservices() {
    return _subservices;
  }

  @JsonProperty(PROP_ENABLED)
  public void setEnabled(@Nullable Boolean enabled) {
    _enabled = enabled;
  }

  @JsonProperty(PROP_SUBSERVICES)
  public void setSubservices(@Nullable SortedMap<String, Service> subservices) {
    _subservices = firstNonNull(subservices, new TreeMap<>());
  }
}
