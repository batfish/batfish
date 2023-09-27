package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;

/** Subcondition checking existence of a route in a table */
@ParametersAreNonnullByDefault
public final class IfRouteExists implements Serializable {

  public @Nullable Prefix getPrefix() {
    return _prefix;
  }

  public void setPrefix(@Nullable Prefix prefix) {
    _prefix = prefix;
  }

  public @Nullable String getTable() {
    return _table;
  }

  public void setTable(@Nullable String table) {
    _table = table;
  }

  private @Nullable Prefix _prefix;
  private @Nullable String _table;
}
