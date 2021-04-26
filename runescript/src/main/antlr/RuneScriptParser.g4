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
    | BOOLEAN_LITERAL   # BooleanLiteral
    | CHAR_LITERAL      # CharacterLiteral
    | NULL_LITERAL      # NullLiteral
    ;

identifier
    : IDENTIFIER
    | BOOLEAN_LITERAL
    | NULL_LITERAL
    | identifier COLON identifier
    ;
