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
   if [ "$lastopt" = "-blocknames" ]; then
      COMPREPLY=( $(compgen -W "$($GNU_FIND ${BATFISH_PATH}/src/org/batfish/logic/libbatfish -name '*_rules.logic' | sed -e 's/\/[^\n]*\///g' -e 's/_rules.logic//g' | tr '\n' ' ')" -- $cur) )
   else
      COMPREPLY=( $(compgen -fW "$(cat $BATFISH_COMPLETION_FILE)" -- $cur) )
   fi
}
export BATFISH_COMPLETION_FILE=$BATFISH_TOOLS_PATH/completion.tmp
complete -F _batfish batfish
