#!/usr/bin/env bash
_batfish()
{
   local cur=${COMP_WORDS[COMP_CWORD]}
   local prev=${COMP_WORDS[COMP_CWORD-1]}
   for i in ${COMP_WORDS[@]:0:COMP_CWORD}; do
      if [[ "$i" = -* ]]; then
         local lastopt="$i"
      fi
   done
   if [ "$prev" = "-loglevel" ]; then
      COMPREPLY=( $(compgen -W "$(grep 'private static final String LEVELSTR' ${COMMON_PATH}/src/org/batfish/common/BatfishLogger.java | sed 's/.* = "\([^"]*\)".*$/\1/g' | tr '\n' ' ')" -- ${cur}) )
   else
      COMPREPLY=( $(compgen -fW "$(cat ${BATFISH_COMPLETION_FILE})" -- ${cur}) )
   fi
}
complete -F _batfish batfish

