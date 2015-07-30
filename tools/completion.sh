#!/usr/bin/env bash
_batfish()
{
    local cur=${COMP_WORDS[COMP_CWORD]}
    COMPREPLY=( $(compgen -W "$(cat $BATFISH_COMPLETION_FILE)" -- $cur) )
}
export BATFISH_COMPLETION_FILE=$BATFISH_TOOLS_PATH/completion.tmp
complete -fF _batfish batfish
