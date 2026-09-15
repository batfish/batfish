package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.flatjuniper.ConfigurationBuilder.unquote;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.S_groups_namedContext;

/**
 * Flat Juniper pre-processor that records the {@code set groups} lines defining each group.
 *
 * <p>This runs before deactivated lines are pruned, since a group all of whose statements are
 * inactive still exists and may still be applied.
 */
@ParametersAreNonnullByDefault
public class GroupDefinitionRecorder extends FlatJuniperParserBaseListener {

  private final @Nonnull Hierarchy _hierarchy;

  public GroupDefinitionRecorder(Hierarchy hierarchy) {
    _hierarchy = hierarchy;
  }

  @Override
  public void exitS_groups_named(S_groups_namedContext ctx) {
    String groupName = unquote(ctx.name.getText()).orElse(ctx.name.getText());
    _hierarchy.addGroupDefinition(groupName, ctx);
  }
}
