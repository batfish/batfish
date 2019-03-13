package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;

/** A line to be matched in order within an {@link AccessList}. */
@ParametersAreNonnullByDefault
public class AccessListLine implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull LineAction _action;
  private final @Nonnull Prefix _prefix;
  private final @Nonnull String _text;

  public AccessListLine(LineAction action, Prefix prefix, String text) {
    _action = action;
    _prefix = prefix;
    _text = text;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }

  public @Nonnull String getText() {
    return _text;
  }
}
