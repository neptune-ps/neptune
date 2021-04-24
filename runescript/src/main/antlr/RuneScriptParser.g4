parser grammar RuneScriptParser;

@lexer::header {package me.filby.neptune.runescript.antlr;}

options { tokenVocab = RuneScriptLexer; }

file
    : script* EOF
    ;

script
    : LBRACK trigger=IDENTIFIER COMMA name=IDENTIFIER RBRACK
    ;
