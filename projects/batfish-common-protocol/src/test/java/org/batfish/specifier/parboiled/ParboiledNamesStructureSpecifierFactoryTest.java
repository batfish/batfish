package org.batfish.specifier.parboiled;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.questions.NamedStructurePropertySpecifier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link ParboiledNamedStructureSpecifierFactory} */
public class ParboiledNamesStructureSpecifierFactoryTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testBuildNamedStructureSpecifierBadInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Error parsing");
    new ParboiledNamedStructureSpecifierFactory().buildNamedStructureSpecifier("@..");
  }

  @Test
  public void testBuildNamedStructureSpecifierGoodInput() {
    assertThat(
        new ParboiledNamedStructureSpecifierFactory()
            .buildNamedStructureSpecifier(NamedStructurePropertySpecifier.ROUTING_POLICY),
        equalTo(
            new ParboiledNamedStructureSpecifier(
                new TypeNamedStructureAstNode(NamedStructurePropertySpecifier.ROUTING_POLICY))));
  }

  @Test
  public void testBuildNamedStructureSpecifierNoInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("requires String input");
    new ParboiledNamedStructureSpecifierFactory().buildNamedStructureSpecifier(null);
  }
}
