package org.batfish.symbolic.interpreter;

import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Prefix;
import org.batfish.symbolic.bdd.BDDTransferFunction;

public interface IAbstractDomain<T> {

  public T init(String router, Set<Prefix> prefixes);

  public T transform(T input, BDDTransferFunction f);

  public T join(T x, T y);

  public BDD finalize(T value);
}
