package org.batfish.datamodel.isp_configuration;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;

/**
 * Represents announcements that modeled ISPs make. Currently, it only has the prefix, which will be
 * announced unconditionally.
 */
@ParametersAreNonnullByDefault
public class IspAnnouncement {

  private static final String PROP_PREFIX = "prefix";

  private final Prefix _prefix;

  @JsonCreator
  private static IspAnnouncement jsonCreator(@JsonProperty(PROP_PREFIX) @Nullable Prefix prefix) {
    checkArgument(
        prefix != null,
        "%s is missing from %s",
        PROP_PREFIX,
        IspAnnouncement.class.getSimpleName());
    return new IspAnnouncement(prefix);
  }

  public IspAnnouncement(Prefix prefix) {
    _prefix = prefix;
  }

  public Prefix getPrefix() {
    return _prefix;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IspAnnouncement)) {
      return false;
    }
    IspAnnouncement that = (IspAnnouncement) o;
    return Objects.equals(_prefix, that._prefix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_prefix);
  }
}
