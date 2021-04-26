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
BOOLEAN_LITERAL : 'true' | 'false' ;
NULL_LITERAL    : 'null' ;
INTEGER_LITERAL : [0-9]+ ;

IDENTIFIER
    : [a-zA-Z0-9_+]+
    ;

WHITESPACE
    : [ \t\n\r]+ -> channel(HIDDEN)
    ;

UNKNOWN
	: . -> channel(HIDDEN)
	;
