parser grammar RuneScriptParser;

@lexer::header {package me.filby.neptune.runescript.antlr;}

options { tokenVocab = RuneScriptLexer; }

scriptFile
    : script* EOF
    ;

script
    : LBRACK trigger=identifier COMMA name=identifier RBRACK
    ;

literal
    : INTEGER_LITERAL   # IntegerLiteral
    | (FALSE | TRUE)    # BooleanLiteral
    ;

identifier
    : IDENTIFIER
    | identifier COLON identifier
    ;
