rmdir /s /q Lazyfier
del *.exe

C:\Python27\python.exe setup.py py2exe

copy /y "..\startup.wav" "dist\startup.wav"
copy /y "..\theend.wav" "dist\theend.wav"
copy /y "..\icon_chat.gif" "dist\icon_chat.gif"
copy /y "..\icon_shutdown.gif" "dist\icon_shutdown.gif"
copy /y "..\icon_info.gif" "dist\icon_info.gif"
copy /y "..\launcher.ico" "dist\launcher.ico"
copy /y "..\kill_server.bat" "dist\kill_server.bat"
copy /y "..\restart_server.bat" "dist\restart_server.bat"
copy /y "..\..\README.md" "dist\readme.txt"

move dist Lazyfier

rmdir /s /q build


"C:\Program Files (x86)\Inno Setup 5\compil32.exe" /cc installer_script.iss


