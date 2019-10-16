package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

/**
 * Contains either name of the redist profile referenced by a redist rule or the prefix used in
 * redist rule
 */
public final class RedistRuleRefNameOrPrefix implements Serializable {

  public RedistRuleRefNameOrPrefix(@Nonnull Prefix prefix) {
    _prefix = prefix;
    _redistProfileName = null;
  }

  public RedistRuleRefNameOrPrefix(@Nonnull String redistProfileName) {
    _prefix = null;
    _redistProfileName = redistProfileName;
  }

  public @Nullable String getRedistProfileName() {
    return _redistProfileName;
  }

  public @Nullable Prefix getPrefix() {
    return _prefix;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RedistRuleRefNameOrPrefix)) {
      return false;
    }
    RedistRuleRefNameOrPrefix that = (RedistRuleRefNameOrPrefix) o;
    return Objects.equals(_redistProfileName, that._redistProfileName)
        && Objects.equals(_prefix, that._prefix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_redistProfileName, _prefix);
  }

  private final @Nullable String _redistProfileName;
  private final @Nullable Prefix _prefix;
}
