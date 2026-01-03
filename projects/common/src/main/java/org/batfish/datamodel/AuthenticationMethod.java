package org.batfish.datamodel;

public enum AuthenticationMethod {
  ENABLE,
  GROUP_RADIUS,
  GROUP_TACACS,
  GROUP_USER_DEFINED,
  KRB5,
  KRB5_TELNET,
  LINE,
  LOCAL,
  LOCAL_CASE,
  NONE,
  PASSWORD, // juniper's name for LOCAL
  UNKNOWN;

  public static AuthenticationMethod toAuthenticationMethod(String method) {
    String methodLowerCase = method.toLowerCase();
    if (methodLowerCase.equals("enable")) {
      return ENABLE;
    } else if (methodLowerCase.equals("krb5")) {
      return KRB5;
    } else if (methodLowerCase.equals("krb5-telnet")) {
      return KRB5_TELNET;
    } else if (methodLowerCase.equals("line")) {
      return LINE;
    } else if (methodLowerCase.equals("local")) {
      return LOCAL;
    } else if (methodLowerCase.equals("local-case")) {
      return LOCAL_CASE;
    } else if (methodLowerCase.equals("none")) {
      return NONE;
    } else if (methodLowerCase.equals("password")) {
      return PASSWORD;
    } else if (methodLowerCase.equals("groupradius") || methodLowerCase.equals("radius")) {
      return GROUP_RADIUS;
    } else if (methodLowerCase.equals("grouptacacs+") || methodLowerCase.equals("tacplus")) {
      return GROUP_TACACS;
    } else if (methodLowerCase.startsWith("group")) {
      return GROUP_USER_DEFINED;
    } else {
      return UNKNOWN;
    }
  }
}
