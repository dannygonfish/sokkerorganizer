ren languages\%1.txt tmp_.txt
@if "%2"=="" GoTo NoEncoding
native2ascii -encoding %2 languages\tmp_.txt languages\%1.txt
@GoTo Fin
:NOENCODING
native2ascii languages\tmp_.txt languages\%1.txt
:FIN
del languages\tmp_.txt
