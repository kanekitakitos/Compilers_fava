grammar EXER1;

prog   : s EOF;

s   : LPAREN l RPAREN
    | 'a'
    ;

l   : s m  // (',' s)*
    ;

m   : VIRGULA s m
    |
    ;

VIRGULA     : ',' ;
A    : 'a' ;
LPAREN   : '(' ;
RPAREN   : ')' ;
WS       : [ \r\t\n]+ -> skip ;
