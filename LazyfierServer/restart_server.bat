
taskkill /f /im LazyfierServer.exe

timeout /t 1

SET mypath=%~dp0
%mypath%LazyfierServer.exe

