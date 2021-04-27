lexer grammar RuneScriptLexer;

// symbols
COLON   : ':' ;
COMMA   : ',' ;
LBRACK  : '[' ;
RBRACK  : ']' ;
PLUS    : '+' ;
MINUS   : '-' ;

// keywords

// literals
INTEGER_LITERAL : [0-9]+ ;
HEX_LITERAL     : '0' [xX] [0-9a-fA-F]+ ;
BOOLEAN_LITERAL : 'true' | 'false' ;
CHAR_LITERAL    : '\'' ~['\\\r\n] '\'' ; // TODO escaping
NULL_LITERAL    : 'null' ;

IDENTIFIER
    : [a-zA-Z0-9_+]+
    ;

WHITESPACE
    : [ \t\n\r]+ -> channel(HIDDEN)
    ;
