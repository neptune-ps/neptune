parser grammar RuneScriptParser;

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
    : IF LPAREN condition RPAREN statement (ELSE statement)?
    ;

whileStatement
    : WHILE LPAREN condition RPAREN statement
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
singleExpression
    : expression EOF
    ;

expression
    : parenthesis                                                               # ParenthesizedExpression
    | calc                                                                      # CalcExpression
    | call                                                                      # CallExpression
    | localVariable                                                             # LocalVariableExpression
    | localArrayVariable                                                        # LocalArrayVariableExpression
    | gameVariable                                                              # GameVariableExpression
    | constantVariable                                                          # ConstantVariableExpression
    | literal                                                                   # LiteralExpression
    | joinedString                                                              # JoinedStringExpression
    | identifier                                                                # IdentifierExpression
    ;

condition
    : LPAREN condition RPAREN                                                   # ConditionParenthesizedExpression
    | condition op=(LT | GT | LTE | GTE) condition                              # ConditionBinaryExpression
    | condition op=(EQ | EXCL) condition                                        # ConditionBinaryExpression
    | condition op=AND condition                                                # ConditionBinaryExpression
    | condition op=OR condition                                                 # ConditionBinaryExpression
    | expression                                                                # ConditionNormalExpression
    ;

calc
    : CALC LPAREN arithemitic RPAREN
    ;

arithemitic
    : LPAREN arithemitic RPAREN                                                 # ArithemiticParenthesizedExpression
    | arithemitic op=(MUL | DIV | MOD) arithemitic                              # ArithemiticBinaryExpression
    | arithemitic op=(PLUS | MINUS) arithemitic                                 # ArithemiticBinaryExpression
    | arithemitic op=AND arithemitic                                            # ArithemiticBinaryExpression
    | arithemitic op=OR arithemitic                                             # ArithemiticBinaryExpression
    | expression                                                                # ArithemiticNormalExpression
    ;

call
    : identifier LPAREN expressionList? RPAREN                                  # CommandCallExpression
    | TILDE identifier (LPAREN expressionList? RPAREN)?                         # ProcCallExpression
    | AT identifier (LPAREN expressionList? RPAREN)?                            # JumpCallExpression
    ;

// invoked manually to parse clientscript references (e.g. cc_setonop)
clientScript
    : identifier (LPAREN args=expressionList? RPAREN)? (LBRACE triggers=expressionList? RBRACE)? EOF
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
    | DEFAULT
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
