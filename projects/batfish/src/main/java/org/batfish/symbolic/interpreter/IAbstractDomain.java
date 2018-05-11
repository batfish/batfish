package org.batfish.symbolic.interpreter;

import java.util.Set;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Prefix;
import org.batfish.symbolic.Protocol;

public interface IAbstractDomain<T> {

  public T init(String router, Protocol proto, @Nullable Set<Prefix> prefixes);

  public T transform(T input, EdgeTransformer f);

  public T merge(T x, T y);

  public BDD finalize(T value);
}
