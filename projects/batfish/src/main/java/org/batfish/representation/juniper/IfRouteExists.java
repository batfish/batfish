package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;

/** Subcondition checking existence of a route in a table */
@ParametersAreNonnullByDefault
public final class IfRouteExists implements Serializable {

  public @Nullable Prefix getPrefix() {
    return _prefix;
  }

  public @Nullable Prefix6 getPrefix6() {
    return _prefix6;
  }

  public void setPrefix(@Nullable Prefix prefix) {
    _prefix = prefix;
    _prefix6 = null;
  }

  public void setPrefix6(@Nullable Prefix6 prefix) {
    _prefix6 = prefix;
    _prefix = null;
  }

  public @Nullable String getTable() {
    return _table;
  }

  public void setTable(@Nullable String table) {
    _table = table;
  }

  private @Nullable Prefix _prefix;
  private @Nullable Prefix6 _prefix6;
  private @Nullable String _table;
}
