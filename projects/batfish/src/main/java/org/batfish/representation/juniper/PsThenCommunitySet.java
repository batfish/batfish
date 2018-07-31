package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.datamodel.routing_policy.statement.SetCommunity;
import org.batfish.datamodel.routing_policy.statement.Statement;

public final class PsThenCommunitySet extends PsThen {

  /** */
  private static final long serialVersionUID = 1L;

  private JuniperConfiguration _configuration;

  private final String _name;

  public PsThenCommunitySet(String name, JuniperConfiguration configuration) {
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
    } else {
      org.batfish.datamodel.CommunityList list = c.getCommunityLists().get(_name);
      // assuming this is a valid community list for setting, the regex value
      // just retrieved should just be an explicit community
      long community = list.getLines().get(0).getMatchCondition().asLiteralCommunities(null).first();
      statements.add(new SetCommunity(new LiteralCommunity(community)));
    }
  }

  public String getName() {
    return _name;
  }
}
