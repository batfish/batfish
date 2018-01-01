package org.batfish.datamodel.questions;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.questions.NodesSpecifier.Type;
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
    NodesSpecifier specifier = new NodesSpecifier("role:svr.*");
    assertThat(specifier.getType(), equalTo(Type.ROLE));
    assertThat(specifier.getRegex().pattern(), equalTo(Pattern.compile("svr.*").pattern()));
  }

  @Test
  public void getMatchingNodesName() {
    NodesSpecifier specifier = new NodesSpecifier("name:lhr-.*");

    Map<String, Configuration> configurations = new HashMap<>();
    String matchingRouter = "lhr-border1";
    String nonMatchingRouter1 = "svr-border1";
    String nonMatchingRouter2 = "svr-border2";
    Configuration matching = new Configuration(matchingRouter, ConfigurationFormat.UNKNOWN);
    Configuration nonMatching1 = new Configuration(nonMatchingRouter1, ConfigurationFormat.UNKNOWN);
    Configuration nonMatching2 = new Configuration(nonMatchingRouter2, ConfigurationFormat.UNKNOWN);
    nonMatching2.getRoles().add("lhr-border1"); // to check for accidental role matching
    configurations.put(matchingRouter, matching);
    configurations.put(nonMatchingRouter1, nonMatching1);
    configurations.put(nonMatchingRouter2, nonMatching2);

    Set<String> matchingNodes = specifier.getMatchingNodes(configurations);

    assertThat(matchingNodes, hasItem(matchingRouter));
    assertThat(matchingNodes, not(hasItem(nonMatchingRouter1)));
    assertThat(matchingNodes, not(hasItem(nonMatchingRouter2)));
  }

  @Test
  public void getMatchingNodesRole() {
    NodesSpecifier specifier = new NodesSpecifier("role:svr.*");

    Map<String, Configuration> configurations = new HashMap<>();
    String matchingRouter = "lhr-border1";
    String nonMatchingRouter1 = "svr-border1"; // name shouldn't match role
    String nonMatchingRouter2 = "lhr-border2";
    Configuration matching = new Configuration(matchingRouter, ConfigurationFormat.UNKNOWN);
    matching.getRoles().add("svr-web");
    Configuration nonMatching1 = new Configuration(nonMatchingRouter1, ConfigurationFormat.UNKNOWN);
    matching.getRoles().add("web");
    Configuration nonMatching2 = new Configuration(nonMatchingRouter2, ConfigurationFormat.UNKNOWN);
    nonMatching2.getRoles().add("rtr");
    configurations.put(matchingRouter, matching);
    configurations.put(nonMatchingRouter1, nonMatching1);
    configurations.put(nonMatchingRouter2, nonMatching2);

    Set<String> matchingNodes = specifier.getMatchingNodes(configurations);

    assertThat(matchingNodes, hasItem(matchingRouter));
    assertThat(matchingNodes, not(hasItem(nonMatchingRouter1)));
    assertThat(matchingNodes, not(hasItem(nonMatchingRouter2)));
  }
}
