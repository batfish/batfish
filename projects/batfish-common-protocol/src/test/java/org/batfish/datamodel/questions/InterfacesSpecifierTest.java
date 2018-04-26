package org.batfish.datamodel.questions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.regex.Pattern;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.questions.InterfacesSpecifier.Type;
import org.junit.Test;

public class InterfacesSpecifierTest {

  @Test
  public void constructorImplicitName() {
    InterfacesSpecifier specifier = new InterfacesSpecifier("Loopback.*");
    assertThat(specifier.getType(), equalTo(Type.NAME));
    assertThat(specifier.getRegex().pattern(), equalTo(Pattern.compile("Loopback.*").pattern()));
  }

  @Test
  public void constructorExplicitName() {
    InterfacesSpecifier specifier = new InterfacesSpecifier("name:Loopback.*");
    assertThat(specifier.getType(), equalTo(Type.NAME));
    assertThat(specifier.getRegex().pattern(), equalTo(Pattern.compile("Loopback.*").pattern()));
  }

  @Test
  public void constructorRole() {
    InterfacesSpecifier specifier = new InterfacesSpecifier("desc:secret.*");
    assertThat(specifier.getType(), equalTo(Type.DESC));
    assertThat(specifier.getRegex().pattern(), equalTo(Pattern.compile("secret.*").pattern()));
  }

  @Test
  public void matchesName() {
    InterfacesSpecifier specifier = new InterfacesSpecifier("name:Loopback.*");

    Interface loopbackInterface = new Interface("Loopback0");
    Interface nonLoopbackInterface = new Interface("Ethetnet0/0");

    assertThat(specifier.matches(loopbackInterface), equalTo(true));
    assertThat(specifier.matches(nonLoopbackInterface), equalTo(false));
  }

  @Test
  public void matchesDesc() {
    InterfacesSpecifier specifier = new InterfacesSpecifier("desc:secret.*");

    Interface secretInterface = new Interface("Loopback0");
    secretInterface.setDescription("secrets are never secrets for long");
    Interface nonSecretInterface = new Interface("Ethetnet0/0");
    nonSecretInterface.setDescription("this interface couldn't keep its secret");

    assertThat(specifier.matches(secretInterface), equalTo(true));
    assertThat(specifier.matches(nonSecretInterface), equalTo(false));
  }

  @Test
  public void matchesVrf() {
    InterfacesSpecifier specifier = new InterfacesSpecifier("vrf:vrf1");
    Interface vrf1Iface = Interface.builder().setName("iface").setVrf(new Vrf("vrf1")).build();
    Interface vrf2Iface = Interface.builder().setName("iface").setVrf(new Vrf("vrf2")).build();
    assertThat(specifier.matches(vrf1Iface), equalTo(true));
    assertThat(specifier.matches(vrf2Iface), equalTo(false));
  }
}
