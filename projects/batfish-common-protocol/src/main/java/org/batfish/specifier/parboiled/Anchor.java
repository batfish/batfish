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
    ADDRESS_GROUP_NAME,
    CHAR_LITERAL,
    DEPRECATED, // grammar rules that are deprecated
    EOI,
    FILTER_NAME,
    FILTER_NAME_REGEX,
    IGNORE, // grammar rules that shouldn't be the basis for autocompletion
    INTERFACE_GROUP_NAME,
    INTERFACE_NAME,
    INTERFACE_NAME_REGEX,
    INTERFACE_TYPE,
    IP_ADDRESS,
    IP_ADDRESS_MASK,
    IP_PREFIX,
    IP_PROTOCOL_NUMBER,
    IP_RANGE,
    IP_WILDCARD,
    NODE_AND_INTERFACE,
    NODE_NAME,
    NODE_NAME_REGEX,
    NODE_ROLE_DIMENSION_NAME,
    NODE_ROLE_NAME,
    NODE_TYPE,
    REFERENCE_BOOK_AND_ADDRESS_GROUP,
    REFERENCE_BOOK_AND_INTERFACE_GROUP,
    REFERENCE_BOOK_NAME,
    ROUTING_POLICY_NAME,
    ROUTING_POLICY_NAME_REGEX,
    STRING_LITERAL,
    VRF_NAME,
    WHITESPACE,
    ZONE_NAME
  }

  Type value();
}
