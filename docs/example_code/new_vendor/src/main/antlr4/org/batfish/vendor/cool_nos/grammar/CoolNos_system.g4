parser grammar CoolNos_system;

import CoolNos_common;

s_system
:
  SYSTEM
  (
    ssy_host_name
    | ssy_login_banner
  )
;

ssy_host_name: HOST_NAME hostname = host_name NEWLINE;

ssy_login_banner: LOGIN_BANNER banner = string NEWLINE;

host_name
:
  // The extractor will validate that the string:
  // - contains a max of 32 characters
  // - contains only letters, numbers, underscores, dashes, and periods
  // - does not start or end with a period.
  // Note that all of this could be enforced in the lexer using a token, but we choose to do so
  // in the extractor for didactic purposes.
  // See toString(ParserRuleContext messageCtx, Host_nameContext ctx) in CoolNosConfigurationBuilder
  string
;