package org.batfish.symbolic.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.JFactory;
import org.junit.Before;
import org.junit.Test;

public class BDDOpsTest {
  private BDDFactory _factory;
  private BDDOps _bddOps;

  @Before
  public void init() {
    _factory = JFactory.init(10000, 1000);
    _factory.disableReorder();
    _factory.setCacheRatio(64);
    _bddOps = new BDDOps(_factory);
  }

  @Test
  public void testAnd_null() {
    assertThat(_bddOps.and(), equalTo(_factory.one()));
    assertThat(_bddOps.and(null, null), equalTo(_factory.one()));
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
  public void testOr_null() {
    assertThat(_bddOps.or(), equalTo(_factory.zero()));
    assertThat(_bddOps.or(null, null), equalTo(_factory.zero()));
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
}
