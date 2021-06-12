package org.batfish.datamodel.tracking;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;

/** Tracks whether a specified interface is active or not */
public class TrackInterface implements TrackMethod {
  private static final String PROP_TRACKED_INTERFACE = "trackedInterface";

  private final String _trackedInterface;

  @JsonCreator
  public TrackInterface(@JsonProperty(PROP_TRACKED_INTERFACE) @Nonnull String trackedInterface) {
    _trackedInterface = trackedInterface;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TrackInterface)) {
      return false;
    }
    return _trackedInterface.equals(((TrackInterface) obj)._trackedInterface);
  }

  @JsonProperty(PROP_TRACKED_INTERFACE)
  public @Nonnull String getTrackedInterface() {
    return _trackedInterface;
  }

  @Override
  public int hashCode() {
    return _trackedInterface.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(getClass()).add(PROP_TRACKED_INTERFACE, _trackedInterface).toString();
  }

  @Override
  public boolean evaluate(Configuration configuration) {
    Interface trackedInterface = configuration.getAllInterfaces().get(this.getTrackedInterface());
    if (trackedInterface == null) {
      // Assume an undefined interface cannot trigger this track methood
      return false;
    }
    return !trackedInterface.getActive() || trackedInterface.getBlacklisted();
  }
}
