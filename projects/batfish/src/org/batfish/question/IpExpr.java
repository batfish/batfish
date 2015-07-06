package org.batfish.question;

import org.batfish.representation.Ip;

public interface IpExpr {

   public Ip evaluate(Environment environment);

}
