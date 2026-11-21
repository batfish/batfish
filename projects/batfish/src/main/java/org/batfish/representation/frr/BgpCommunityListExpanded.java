package org.batfish.representation.frr;

import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;

/** A expanded Ip community list */
public class BgpCommunityListExpanded extends BgpCommunityList {

  private final LineAction _action;
  private final @Nonnull SortedMap<Long, BgpCommunityListExpandedLine> _lines;

  public BgpCommunityListExpanded(String name, LineAction action) {
    super(name);
    _action = action;
    _lines = null;
  }

  public BgpCommunityListExpanded(String name) {
    super(name);
    _action = null;
    _lines = new TreeMap<>();
  }

  @Override
  public <T> T accept(BgpCommunityListVisitor<T> visitor) {
    return visitor.visitBgpCommunityListExpanded(this);
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  /** The lines of this list, by sequence number. */
  public @Nonnull SortedMap<Long, BgpCommunityListExpandedLine> getLines() {
    return _lines;
  }
}
