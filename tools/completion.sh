#!/usr/bin/env bash
_batfish()
{
    local cur=${COMP_WORDS[COMP_CWORD]}
    COMPREPLY=( $(compgen -W "$(batfish -help | grep -o '^ *-[a-zA-Z0-9]*' | tr -d ' ' | tr '\n' ' ')" -- $cur) )
}
complete -fF _batfish batfish
