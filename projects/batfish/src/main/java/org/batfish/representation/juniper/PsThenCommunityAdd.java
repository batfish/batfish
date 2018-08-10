package org.batfish.representation.juniper;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.statement.AddCommunity;
import org.batfish.datamodel.routing_policy.statement.Statement;

public final class PsThenCommunityAdd extends PsThen {

  /** */
  private static final long serialVersionUID = 1L;

  private JuniperConfiguration _configuration;

  private final String _name;

  public PsThenCommunityAdd(String name, JuniperConfiguration configuration) {
    _name = name;
    _configuration = configuration;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    CommunityList namedList = _configuration.getCommunityLists().get(_name);
    if (namedList == null) {
      warnings.redFlag("Reference to undefined community: \"" + _name + "\"");
      return;
    } else {
      SortedSet<Long> communities = new TreeSet<>();
      for (CommunityListLine clLine : namedList.getLines()) {
        // assuming that regex here is actually a literal community
        String communityStr = clLine.getRegex();
        long communityLong = CommonUtil.communityStringToLong(communityStr);
        communities.add(communityLong);
      }
      statements.add(new AddCommunity(new LiteralCommunitySet(communities)));
    }
  }

  public String getName() {
    return _name;
  }
}
