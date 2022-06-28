call make zip
@del so_v%1.zip > nul
ren so_vXXX.zip so_v%1.zip
@del so_v%1.exe > nul
ren so_vXXX.exe so_v%1.exe
@rem DEBUG\bin\mkverxml %1 > MORE\version.xml
