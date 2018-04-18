package org.batfish.datamodel.questions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.regex.Pattern;
import org.batfish.datamodel.Ip6AccessList;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.questions.FiltersSpecifier.Type;
import org.junit.Test;

public class FiltersSpecifierTest {

  @Test
  public void constructorImplicitName() {
    FiltersSpecifier specifier = new FiltersSpecifier("acl.*");
    assertThat(specifier.getType(), equalTo(Type.NAME));
    assertThat(specifier.getRegex().pattern(), equalTo(Pattern.compile("acl.*").pattern()));
  }

  @Test
  public void constructorExplicitName() {
    FiltersSpecifier specifier = new FiltersSpecifier("name:acl.*");
    assertThat(specifier.getType(), equalTo(Type.NAME));
    assertThat(specifier.getRegex().pattern(), equalTo(Pattern.compile("acl.*").pattern()));
  }

  @Test
  public void constructorIpv4() {
    FiltersSpecifier specifier = new FiltersSpecifier("ipv4:secret.*");
    assertThat(specifier.getType(), equalTo(Type.IPV4));
    assertThat(specifier.getRegex().pattern(), equalTo(Pattern.compile("secret.*").pattern()));
  }

  @Test
  public void matchesName() {
    FiltersSpecifier specifier = new FiltersSpecifier("name:acl.*");

    IpAccessList matchingList = new IpAccessList("acl-99");
    IpAccessList nonMatchingList = new IpAccessList("nana");

    assertThat(specifier.matches(matchingList), equalTo(true));
    assertThat(specifier.matches(nonMatchingList), equalTo(false));
  }

  @Test
  public void matchesIpv4() {
    FiltersSpecifier specifier = new FiltersSpecifier("ipv4:acl.*");

    IpAccessList matchingList = new IpAccessList("acl-99");
    Ip6AccessList nonMatchingList = new Ip6AccessList("acl-99");

    assertThat(specifier.matches(matchingList), equalTo(true));
    assertThat(specifier.matches(nonMatchingList), equalTo(false));
  }
}
