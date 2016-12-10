#!/bin/sh

cd "$(dirname "$0")"

cd ..

/usr/bin/python3 ../../PyInstaller/pyinstaller.py -F LazyfierServer.py

cp -f startup.wav dist/startup.wav
cp -f theend.wav dist/theend.wav
cp -f *.png dist/
cp -f *.gif dist/
cp -f lazyfier-restart dist/lazyfier-restart
cp -f ../README.md dist/readme.txt

rm -f *.spec
rm -fr build/
rm -fr __pycache__/
rm -fr linux_setup/lazyfier-6.1/opt/lazyfier_6/

mv dist/ linux_setup/lazyfier-6.1/opt/lazyfier_6/

chmod -R 755 linux_setup/lazyfier-6.1/DEBIAN/

dpkg-deb --build ./linux_setup/lazyfier-6.1/

