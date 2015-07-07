package org.batfish.question;

import org.batfish.representation.Ip;

public interface IpExpr extends PrintableExpr {

   public Ip evaluate(Environment environment);

}
