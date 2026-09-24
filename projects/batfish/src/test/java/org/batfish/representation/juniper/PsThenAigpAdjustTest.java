package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.junit.Test;

public final class PsThenAigpAdjustTest {

  @Test
  public void testAccessors() {
    PsThenAigpAdjust aigpAdjust =
        new PsThenAigpAdjust(PsThenAigpAdjust.Operator.ADD, BigInteger.ONE);

    assertThat(aigpAdjust.getOperator(), equalTo(PsThenAigpAdjust.Operator.ADD));
    assertThat(aigpAdjust.getValue(), equalTo(BigInteger.ONE));
  }

  @Test
  public void testConversion() {
    List<Statement> statements = new ArrayList<>();
    new PsThenAigpAdjust(PsThenAigpAdjust.Operator.ADD, null)
        .applyTo(
            statements,
            new JuniperConfiguration(),
            Configuration.builder()
                .setConfigurationFormat(ConfigurationFormat.JUNIPER)
                .setHostname("host")
                .build(),
            new Warnings());
    assertThat(statements, empty());
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new PsThenAigpAdjust(PsThenAigpAdjust.Operator.ADD, null),
            new PsThenAigpAdjust(PsThenAigpAdjust.Operator.ADD, null))
        .addEqualityGroup(new PsThenAigpAdjust(PsThenAigpAdjust.Operator.ADD, BigInteger.ONE))
        .addEqualityGroup(new PsThenAigpAdjust(PsThenAigpAdjust.Operator.SUBTRACT, BigInteger.ONE))
        .testEquals();
  }

  @Test
  public void testLastWins() {
    PsThens thens = new PsThens();
    thens.addPsThen(new PsThenAigpAdjust(PsThenAigpAdjust.Operator.ADD, BigInteger.ONE));
    assertThat(
        thens.addPsThen(new PsThenAigpAdjust(PsThenAigpAdjust.Operator.SUBTRACT, BigInteger.TWO)),
        contains("aigp-adjust"));
    assertThat(
        thens.getAllThens(),
        contains(new PsThenAigpAdjust(PsThenAigpAdjust.Operator.SUBTRACT, BigInteger.TWO)));
  }
}
