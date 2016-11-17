Android PC remote controller

I tried several remote controller apps for android to controll my desktop multimedia. 
They generally offer too much and load too slow. I decided to write my own lightweight app and server.
it should work on any Android version above 4.0.3.

  I wrote the server in python for both Linux (tested on XUbuntu but should work on any other version too) and Windows (tested on win7 64-bit and windows 10). The python scrypt is chross platform, but some additional scripts and functions are platform specific so it detects the platform in order to adapt. 
  On windows I used py2exe to wrap the scrypt into an executable. PyInstaller has too many dependencies and makes windows installation more difficult (needs cpp runtime too), but py2exe uses python2, so the script is adjusted for python2 on windows. However, on linux, python2 does not set window icon, so I used python3 and wrapped the server in a binary using PyInstaller.

Features:

1. Emulates multimedial keyboard input:
- PLAY/PAUSE
- STOP
- NEXT
- PREVIOUS
- VOLUME_UP
- VOLUME_DOWN

2. uses it's own script to shut down the computer.

3. emulates mouse (Left click, Right click, Scroll and cursor moovement)

4. If a browser window with YouTube opened has focus, the PLAY button sends "k" and NEXT and PREVIOUS button send "l" and "j" to support youtube commands.

5. Long press on PLAY button sends "space" to support pausing videos in a browser.


If you wish to contribute, send me a feature proposition you would find usefull to yourself. 

More details are on my website, http://radinaradionica.com/en/programming/lazyfier
