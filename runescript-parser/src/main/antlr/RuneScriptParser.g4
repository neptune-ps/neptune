parser grammar RuneScriptParser;

@members {
boolean inCalc = false;
boolean inCondition = false;
}

@lexer::header {package me.filby.neptune.runescript.antlr;}

options { tokenVocab = RuneScriptLexer; }

scriptFile
    : script* EOF
    ;

script
    : LBRACK trigger=identifier COMMA name=identifier RBRACK
      (LPAREN parameterList? RPAREN)?
      (LPAREN typeList? RPAREN)?
      statement*
    ;

parameterList
    : parameter (COMMA parameter)*
    ;

parameter
    : type=(IDENTIFIER | TYPE_ARRAY) DOLLAR advancedIdentifier
    ;

typeList
    : IDENTIFIER (COMMA IDENTIFIER)*
    ;

// statements
statement
    : blockStatement
    | returnStatement
    | ifStatement
    | whileStatement
    | switchStatement
    | declarationStatement
    | arrayDeclarationStatement
    | assignmentStatement
    | expressionStatement
    | emptyStatement
    ;

blockStatement
    : LBRACE statement* RBRACE
    ;

returnStatement
    : RETURN (LPAREN expressionList? RPAREN)? SEMICOLON
    ;

ifStatement
    : IF {inCondition=true;} parenthesis {inCondition=false;} statement (ELSE statement)?
    ;

whileStatement
    : WHILE {inCondition=true;} parenthesis {inCondition=false;} statement
    ;

switchStatement
    : SWITCH_TYPE parenthesis LBRACE switchCase* RBRACE
    ;

switchCase
    : CASE (DEFAULT | expressionList) COLON statement*
    ;

declarationStatement
    : DEF_TYPE DOLLAR advancedIdentifier (EQ expression)? SEMICOLON
    ;

arrayDeclarationStatement
    : DEF_TYPE DOLLAR advancedIdentifier parenthesis SEMICOLON
    ;

assignmentStatement
    : assignableVariableList EQ expressionList SEMICOLON
    ;

expressionStatement
    : expression SEMICOLON
    ;

emptyStatement
    : SEMICOLON
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
    | expression {inCondition}? op=(LT | GT | LTE | GTE) expression             # BinaryExpression
    | expression {inCondition}? op=(EQ | EXCL) expression                       # BinaryExpression
    | expression {inCalc || inCondition}? op=AND expression                     # BinaryExpression
    | expression {inCalc || inCondition}? op=OR expression                      # BinaryExpression
    | {!inCalc}? CALC {inCalc=true;} parenthesis {inCalc=false;}                # CalcExpression
    | call                                                                      # CallExpression
    | localVariable                                                             # LocalVariableExpression
    | localArrayVariable                                                        # LocalArrayVariableExpression
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

// invoked manually to parse clientscript references (e.g. cc_setonop)
clientScript
    : identifier LPAREN args=expressionList? RPAREN (LBRACE triggers=expressionList RBRACE)?
    ;

assignableVariableList
    : assignableVariable (COMMA assignableVariable)*
    ;

assignableVariable
    : localVariable
    | localArrayVariable
    | gameVariable
    ;

localVariable
    : DOLLAR advancedIdentifier
    ;

localArrayVariable
    : DOLLAR advancedIdentifier parenthesis
    ;

gameVariable
    : MOD advancedIdentifier
    ;

constantVariable
    : CARET advancedIdentifier
    ;

literal
    : INTEGER_LITERAL   # IntegerLiteral
    | HEX_LITERAL       # IntegerLiteral
    | COORD_LITERAL     # CoordLiteral
    | BOOLEAN_LITERAL   # BooleanLiteral
    | CHAR_LITERAL      # CharacterLiteral
    | NULL_LITERAL      # NullLiteral
    | stringLiteral     # StringLiteralExpression
    ;

stringLiteral
    : QUOTE_OPEN stringLiteralContent* QUOTE_CLOSE
    ;

stringLiteralContent
    : STRING_TEXT
    | STRING_TAG
    ;

joinedString
    : QUOTE_OPEN (stringLiteralContent | stringExpression)* QUOTE_CLOSE
    ;

stringExpression
    : STRING_EXPR_START expression STRING_EXPR_END
    ;

// simple identifier with limited amount of keywords allowed
identifier
    : IDENTIFIER
    | HEX_LITERAL
    | BOOLEAN_LITERAL
    | NULL_LITERAL
    | TYPE_ARRAY
    | DEF_TYPE
    ;

// advanced identifier that allows more keywords
advancedIdentifier
    : identifier
    | IF
    | ELSE
    | WHILE
    | RETURN
    | CALC
    ;
