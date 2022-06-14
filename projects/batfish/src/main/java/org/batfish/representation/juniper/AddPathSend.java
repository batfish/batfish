package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Add-path send configuration detailing which additional-paths should be sent. */
public final class AddPathSend implements Serializable {

  public @Nullable Integer getPathCount() {
    return _pathCount;
  }

  public void setPathCount(@Nullable Integer pathCount) {
    _pathCount = pathCount;
  }

  public boolean getMultipath() {
    return _multipath;
  }

  public void setMultipath(boolean multipath) {
    _multipath = multipath;
  }

  public @Nullable PathSelectionMode getPathSelectionMode() {
    return _pathSelectionMode;
  }

  public void setPathSelectionMode(@Nullable PathSelectionMode pathSelectionMode) {
    _pathSelectionMode = pathSelectionMode;
  }

  public @Nullable String getPrefixPolicy() {
    return _prefixPolicy;
  }

  public void setPrefixPolicy(@Nullable String prefixPolicy) {
    _prefixPolicy = prefixPolicy;
  }

  private @Nullable Integer _pathCount;
  private boolean _multipath;
  private @Nullable PathSelectionMode _pathSelectionMode;

  private @Nullable String _prefixPolicy;
}
