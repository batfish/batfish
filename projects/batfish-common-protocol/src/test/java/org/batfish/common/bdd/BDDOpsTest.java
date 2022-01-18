package org.batfish.common.bdd;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

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
}
