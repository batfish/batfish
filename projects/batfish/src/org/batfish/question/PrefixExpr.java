package org.batfish.question;

import org.batfish.representation.Prefix;

public interface PrefixExpr extends PrintableExpr {

   public Prefix evaluate(Environment environment);

}
