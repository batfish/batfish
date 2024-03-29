package org.batfish.representation.cumulus_nclu;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;

/** Represents one line of a Cumulus AS-path access list. */
public class IpAsPathAccessListLine implements Serializable {
  private final @Nonnull LineAction _action;
  private final long _asNum;

  public IpAsPathAccessListLine(@Nonnull LineAction action, long asNum) {
    _action = action;
    _asNum = asNum;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public long getAsNum() {
    return _asNum;
  }
}
