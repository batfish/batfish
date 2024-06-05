package org.batfish.common.bdd;

import static org.batfish.common.bdd.BDDOps.mapAndOrAllNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.function.Function;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BDDOpsTest {
  @Rule public ExpectedException exception = ExpectedException.none();

  private BDDFactory _factory;
  private BDDOps _bddOps;

  @Before
  public void init() {
    _factory = JFactory.init(10000, 1000);
    _factory.setCacheRatio(64);
    _bddOps = new BDDOps(_factory);
  }

  @Test
  public void testAnd_one() {
    _factory.setVarNum(1);
    BDD var = _factory.ithVar(0);
    assertThat(_bddOps.and(var, _factory.one()), equalTo(var));
  }

  @Test
  public void testAnd_var_varNot() {
    _factory.setVarNum(1);
    BDD var = _factory.ithVar(0);
    assertThat(_bddOps.and(var, var.not()), equalTo(_factory.zero()));
  }

  @Test
  public void testAnd_zero() {
    _factory.setVarNum(1);
    BDD var = _factory.ithVar(0);
    assertThat(_bddOps.and(var, _factory.zero()), equalTo(_factory.zero()));
  }

  @Test
  public void testOr_one() {
    _factory.setVarNum(1);
    BDD var = _factory.ithVar(0);
    assertThat(_bddOps.or(var, _factory.one()), equalTo(_factory.one()));
  }

  @Test
  public void testOr_var_varNot() {
    _factory.setVarNum(1);
    BDD var = _factory.ithVar(0);
    assertThat(_bddOps.or(var, var.not()), equalTo(_factory.one()));
  }

  @Test
  public void testOr_zero() {
    _factory.setVarNum(1);
    BDD var = _factory.ithVar(0);
    assertThat(_bddOps.or(var, _factory.zero()), equalTo(var));
  }

  @Test
  public void testMapAndOrAllNull_zero() {
    // return null if objects is null or empty
    assertNull(mapAndOrAllNull(null, Function.identity()));
    assertNull(mapAndOrAllNull(ImmutableList.of(), Function.identity()));
  }

  @Test
  public void testMapAndOrAllNull_free() {
    // consumes all inputs
    _factory.setVarNum(3);
    BDD v0 = _factory.ithVar(0);
    BDD v1 = _factory.ithVar(1);
    BDD v2 = _factory.ithVar(2);
    BDD expected = _factory.orAll(v0, v1, v2);
    assertEquals(4, _factory.numOutstandingBDDs());
    BDD actual = mapAndOrAllNull(ImmutableList.of(v0, v1, v2), Function.identity());
    assertEquals(expected, actual);

    // frees its inputs
    assertEquals(2, _factory.numOutstandingBDDs());
  }

  @Test
  public void testMapAndOrAll_zero() {
    // return zero if objects is null or empty
    assertTrue(_bddOps.mapAndOrAll(null, Function.identity()).isZero());
    assertTrue(_bddOps.mapAndOrAll(ImmutableList.of(), Function.identity()).isZero());
  }

  @Test
  public void testMapAndOrAll_free() {
    // consumes all inputs
    _factory.setVarNum(3);
    BDD v0 = _factory.ithVar(0);
    BDD v1 = _factory.ithVar(1);
    BDD v2 = _factory.ithVar(2);
    BDD expected = _factory.orAll(v0, v1, v2);
    assertEquals(4, _factory.numOutstandingBDDs());
    BDD actual = _bddOps.mapAndOrAll(ImmutableList.of(v0, v1, v2), Function.identity());
    assertEquals(expected, actual);

    // frees its inputs
    assertEquals(2, _factory.numOutstandingBDDs());
  }
}
