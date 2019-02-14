package org.batfish.specifier.parboiled;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is applied to rules that we expect to be the basis of error reporting and auto
 * completion. The value of the annotation is the auto completion type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Anchor {

  enum Type {
    ADDRESS_GROUP_AND_BOOK,
    EOI,
    IP_ADDRESS,
    IP_ADDRESS_MASK,
    IP_PREFIX,
    IP_RANGE,
    IP_WILDCARD,
    STRING_LITERAL,
    WHITESPACE
  }

  Type value();
}
