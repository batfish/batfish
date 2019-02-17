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
    INTERFACE_GROUP_AND_BOOK,
    INTERFACE_NAME,
    INTERFACE_NAME_REGEX,
    INTERFACE_TYPE,
    NODE_NAME,
    NODE_NAME_REGEX,
    NODE_ROLE_NAME_AND_DIMENSION,
    NODE_TYPE,
    STRING_LITERAL,
    VRF_NAME,
    WHITESPACE,
    ZONE_NAME
  }

  Type value();
}
