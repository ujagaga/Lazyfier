#!/bin/sh

cd "$(dirname "$0")"

/usr/bin/python3 ../../PyInstaller/pyinstaller.py -F LazyfierServer.py

cp -f startup.wav dist/startup.wav
cp -f theend.wav dist/theend.wav
cp -f *.png dist/
cp -f *.gif dist/
cp -f restart_server.sh dist/restart_server.sh
cp -f ../../README.md dist/readme.txt

rm -f *.spec
rm -fr build/
rm -fr __pycache__/
rm -fr ./deb_package/lazyfier-6.0/opt/lazyfier_6/

mv dist/ deb_package/lazyfier-6.0/opt/lazyfier_6/

chmod -R 755 deb_package/lazyfier-6.0/DEBIAN/

dpkg-deb --build ./deb_package/lazyfier-6.0/

