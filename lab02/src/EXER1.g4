grammar EXER1;

prog   : s EOF;

s   : LPAREN l RPAREN
    | IDENTIDADE
    ;

l   : l VIRGULA s
    | s
    ;

VIRGULA     : ',' ;
IDENTIDADE    : 'a' ;
LPAREN   : '(' ;
RPAREN   : ')' ;
WS       : [ \r\t\n]+ -> skip ;
