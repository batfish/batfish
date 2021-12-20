package org.batfish.representation.frr;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;

/** A expanded Ip community list */
public class BgpCommunityListExpanded extends BgpCommunityList {

  private final LineAction _action;
  private final @Nonnull List<BgpCommunityListExpandedLine> _lines;

  public BgpCommunityListExpanded(String name, LineAction action) {
    super(name);
    _action = action;
    _lines = null;
  }

  public BgpCommunityListExpanded(String name) {
    super(name);
    _action = null;
    _lines = new ArrayList<>();
  }

  @Override
  public <T> T accept(BgpCommunityListVisitor<T> visitor) {
    return visitor.visitBgpCommunityListExpanded(this);
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nonnull List<BgpCommunityListExpandedLine> getLines() {
    return _lines;
  }
}
