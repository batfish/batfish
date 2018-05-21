package org.batfish.symbolic.ainterpreter;

import java.util.Set;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Prefix;
import org.batfish.symbolic.Protocol;

public interface IAbstractDomain<T> {

  T bot();

  T value(String router, Protocol proto, @Nullable Set<Prefix> prefixes);

  T transform(T input, EdgeTransformer f);

  T merge(T x, T y);

  BDD toBdd(T value);
}
