package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;

/** An object with associated text in the INSPECT language. */
public interface HasInspectText {

  @Nonnull
  String getInspectText();
}
