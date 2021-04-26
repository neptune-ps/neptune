lexer grammar RuneScriptLexer;

// symbols
COLON   : ':' ;
COMMA   : ',' ;
LBRACK  : '[' ;
RBRACK  : ']' ;
PLUS    : '+' ;
MINUS   : '-' ;

// keywords
TRUE    : 'true' ;
FALSE   : 'false' ;

INTEGER_LITERAL
    : DIGIT+
    ;

fragment DIGIT
    : [0-9]
    ;

IDENTIFIER
    : [a-zA-Z0-9_+]+
    ;

WHITESPACE
    : [ \t\n\r]+ -> channel(HIDDEN)
    ;

UNKNOWN
	: . -> channel(HIDDEN)
	;
