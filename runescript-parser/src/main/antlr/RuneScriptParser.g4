parser grammar RuneScriptParser;

@members {
boolean inCalc = false;
}

@lexer::header {package me.filby.neptune.runescript.antlr;}

options { tokenVocab = RuneScriptLexer; }

scriptFile
    : script* EOF
    ;

script
    : LBRACK trigger=identifier COMMA name=identifier RBRACK statement*
    ;

// statements
statement
    : expression SEMICOLON                                                      # ExpressionStatement
    ;

expressionList
    : expression (COMMA expression)*
    ;

parenthesis
    : LPAREN expression RPAREN
    ;

// expressions
expression
    : expression {inCalc}? op=(MUL | DIV | MOD) expression                      # BinaryExpression
    | expression {inCalc}? op=(PLUS | MINUS) expression                         # BinaryExpression
    | expression {inCalc}? op=AND expression                                    # BinaryExpression
    | expression {inCalc}? op=OR expression                                     # BinaryExpression
    | {!inCalc}? CALC {inCalc=true;} LPAREN expression RPAREN {inCalc=false;}   # CalcExpression
    | identifier LPAREN expressionList? RPAREN                                  # CallExpression
    | literal                                                                   # LiteralExpression
    | identifier                                                                # IdentifierExpression
    ;

literal
    : INTEGER_LITERAL   # IntegerLiteral
    | HEX_LITERAL       # IntegerLiteral
    | BOOLEAN_LITERAL   # BooleanLiteral
    | CHAR_LITERAL      # CharacterLiteral
    | NULL_LITERAL      # NullLiteral
    ;

identifier
    : IDENTIFIER
    | HEX_LITERAL
    | BOOLEAN_LITERAL
    | NULL_LITERAL
    | CALC
    | identifier COLON identifier
    ;
