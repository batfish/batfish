package org.batfish.representation.juniper;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.communities.CommunitySetReference;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.statement.Statement;

@ParametersAreNonnullByDefault
public final class PsThenCommunityAdd extends PsThen {

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
    if (!c.getCommunitySets().containsKey(_name)) {
      // undefined reference; or not converted because it contains only regexes
      return;
    }
    _configuration.getOrCreateNamedCommunitiesUsedForSet().add(_name);
    statements.add(
        new SetCommunities(
            CommunitySetUnion.of(InputCommunities.instance(), new CommunitySetReference(_name))));
  }

  public @Nonnull String getName() {
    return _name;
  }

  private JuniperConfiguration _configuration;
  private final String _name;
}
