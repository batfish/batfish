package org.batfish.representation.cumulus_nclu;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;

/** A expanded Ip community list */
public class IpCommunityListExpanded extends IpCommunityList {

  private final LineAction _action;
  private final @Nonnull List<IpCommunityListExpandedLine> _lines;

  public IpCommunityListExpanded(String name, LineAction action) {
    super(name);
    _action = action;
    _lines = null;
  }

  public IpCommunityListExpanded(String name) {
    super(name);
    _action = null;
    _lines = new ArrayList<>();
  }

  @Override
  public <T> T accept(IpCommunityListVisitor<T> visitor) {
    return visitor.visitIpCommunityListExpanded(this);
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nonnull List<IpCommunityListExpandedLine> getLines() {
    return _lines;
  }
}
