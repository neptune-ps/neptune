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
    : parenthesis                                                               # ParenthesizedExpression
    | expression {inCalc}? op=(MUL | DIV | MOD) expression                      # BinaryExpression
    | expression {inCalc}? op=(PLUS | MINUS) expression                         # BinaryExpression
    | expression {inCalc}? op=AND expression                                    # BinaryExpression
    | expression {inCalc}? op=OR expression                                     # BinaryExpression
    | {!inCalc}? CALC {inCalc=true;} parenthesis {inCalc=false;}                # CalcExpression
    | call                                                                      # CallExpression
    | localVariable                                                             # LocalVariableExpression
    | gameVariable                                                              # GameVariableExpression
    | constantVariable                                                          # ConstantVariableExpression
    | literal                                                                   # LiteralExpression
    | joinedString                                                              # JoinedStringExpression
    | identifier                                                                # IdentifierExpression
    ;

call
    : identifier LPAREN expressionList? RPAREN                                  # CommandCallExpression
    | TILDE identifier (LPAREN expressionList? RPAREN)?                         # ProcCallExpression
    | AT identifier (LPAREN expressionList? RPAREN)?                            # JumpCallExpression
    ;

localVariable
    : DOLLAR identifier parenthesis?
    ;

gameVariable
    : MOD identifier
    ;

constantVariable
    : CARET identifier
    ;

literal
    : INTEGER_LITERAL   # IntegerLiteral
    | HEX_LITERAL       # IntegerLiteral
    | BOOLEAN_LITERAL   # BooleanLiteral
    | CHAR_LITERAL      # CharacterLiteral
    | NULL_LITERAL      # NullLiteral
    | stringLiteral     # StringLiteralExpression
    ;

stringLiteral
    : QUOTE_OPEN stringLiteralContent* QUOTE_CLOSE
    ;

// TODO tag support
stringLiteralContent
    : STRING_TEXT
//    | STRING_ESCAPED_CHAR
    ;

joinedString
    : QUOTE_OPEN (stringLiteralContent | stringExpression)* QUOTE_CLOSE
    ;

stringExpression
    : STRING_EXPR_START expression STRING_EXPR_END
    ;

identifier
    : IDENTIFIER
    | HEX_LITERAL
    | BOOLEAN_LITERAL
    | NULL_LITERAL
    | CALC
    | identifier COLON identifier
    ;
