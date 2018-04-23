package org.batfish.datamodel.questions;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Pattern;
import org.batfish.datamodel.questions.NodesSpecifier.Type;
import org.batfish.role.NodeRole;
import org.batfish.role.NodeRoleDimension;
import org.junit.Test;

public class NodesSpecifierTest {

  @Test
  public void constructorImplicitName() {
    NodesSpecifier specifier = new NodesSpecifier("lhr-.*");
    assertThat(specifier.getType(), equalTo(Type.NAME));
    assertThat(specifier.getRegex().pattern(), equalTo(Pattern.compile("lhr-.*").pattern()));
  }

  @Test
  public void constructorExplicitName() {
    NodesSpecifier specifier = new NodesSpecifier("name:lhr-.*");
    assertThat(specifier.getType(), equalTo(Type.NAME));
    assertThat(specifier.getRegex().pattern(), equalTo(Pattern.compile("lhr-.*").pattern()));
  }

  @Test
  public void constructorRole() {
    NodesSpecifier specifier = new NodesSpecifier("role:dim:svr.*");
    assertThat(specifier.getType(), equalTo(Type.ROLE));
    assertThat(specifier.getRoleDimension(), equalTo("dim"));
    assertThat(specifier.getRegex().pattern(), equalTo(Pattern.compile("svr.*").pattern()));
  }

  @Test
  public void getMatchingNodesByName() {
    NodesSpecifier specifier = new NodesSpecifier("name:lhr-.*");

    String matchingRouter = "lhr-border1";
    String nonMatchingRouter = "svr-border1";
    Set<String> nodes =
        new ImmutableSet.Builder<String>().add(matchingRouter).add(nonMatchingRouter).build();

    Set<String> matchingNodes = specifier.getMatchingNodesByName(nodes);

    assertThat(matchingNodes, hasItem(matchingRouter));
    assertThat(matchingNodes, not(hasItem(nonMatchingRouter)));
  }

  @Test
  public void getMatchingNodesByRole() {
    NodesSpecifier specifier = new NodesSpecifier("role:dim:match.*");

    String matchingNode1 = "lhr-border-01";
    String matchingNode2 = "svr-border-02";
    String nonMatchingNode1 = "svr-core-1";
    Set<String> nodes =
        new ImmutableSet.Builder<String>()
            .add(matchingNode1)
            .add(matchingNode2)
            .add(nonMatchingNode1)
            .build();

    NodeRole role1 = new NodeRole("match1", "lhr-border.*");
    NodeRole role2 = new NodeRole("match2", "svr-border.*");
    NodeRole role3 = new NodeRole("dumb0", "lhr-core.*");
    SortedSet<NodeRole> roles =
        new ImmutableSortedSet.Builder<NodeRole>(NodeRole::compareTo)
            .add(role1)
            .add(role2)
            .add(role3)
            .build();

    NodeRoleDimension roleDimension = new NodeRoleDimension("dim", roles, null, null);

    Set<String> matchingNodes = specifier.getMatchingNodesByRole(roleDimension, nodes);

    assertThat(matchingNodes, hasItem(matchingNode1));
    assertThat(matchingNodes, hasItem(matchingNode2));
    assertThat(matchingNodes, not(hasItem(nonMatchingNode1)));
  }
}
