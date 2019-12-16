package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.acl.MatchHeaderSpace;

/** A line to be matched in order within an {@link AccessList}. */
@ParametersAreNonnullByDefault
public class AccessListLine implements Serializable {

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

  /**
   * Returns the literal text from the configuration used to produce this {@link AccessListLine}.
   */
  public @Nonnull String getText() {
    return _text;
  }

  /** Convert to vendor-independent {@link ExprAclLine}. */
  public @Nonnull ExprAclLine toIpAccessListLine() {
    return ExprAclLine.builder()
        .setAction(_action)
        .setMatchCondition(
            new MatchHeaderSpace(HeaderSpace.builder().setSrcIps(_prefix.toIpSpace()).build()))
        .setName(_text)
        .build();
  }
}
