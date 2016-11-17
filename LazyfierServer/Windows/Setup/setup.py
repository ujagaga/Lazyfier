# setup.py
from distutils.core import setup
import py2exe

setup(windows=[{"script":"..\LazyfierServer.py", "icon_resources":[(1, "..\launcher.ico")]}])
