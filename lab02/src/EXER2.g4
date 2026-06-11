grammar EXER2;

prog   : s EOF;

s   : A s B s
    | B s A s
    |
    ;



A    : 'a' ;
B    : 'b' ;

WS       : [ \r\t\n]+ -> skip ;
