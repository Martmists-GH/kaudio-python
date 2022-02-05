import os
import sys
import platform
from setuptools import setup, Extension
import sysconfig
import shutil

osname = platform.system()
paths = sysconfig.get_paths()
debug = True

if sys.version_info < (3, 8):
    os.waitstatus_to_exitcode = lambda x: 0

if osname == "Linux":
    gradle_bin = "./gradlew"
else:
    gradle_bin = "./gradlew.bat"


def fill_template():
    with open("src/nativeInterop/cinterop/python.def.template") as fp:
        content = fp.read()
    formatted = content.format(INCLUDE_DIR=paths['platinclude'])
    with open("src/nativeInterop/cinterop/python.def", "w") as fp:
        fp.write(formatted)


def build_gradle():
    if osname == "Linux":
        if os.waitstatus_to_exitcode(os.system(f"{gradle_bin} clean")) != 0 or os.waitstatus_to_exitcode(os.system(f"{gradle_bin} build")) != 0:
            raise Exception("Build failed")


def copy_source():
    if os.path.exists("kaudio/"):
        shutil.rmtree("kaudio/")
    shutil.copytree("src/main/python/", "kaudio/")


def extensions():
    folder = "debugStatic" if debug else "releaseStatic"

    native = Extension('_kaudio',
                       sources=['src/nativeMain/cpp/entrypoint.cpp'],
                       include_dirs=[f"build/bin/native/{folder}/"],
                       library_dirs=[f"build/bin/native/{folder}/"],
                       libraries=["kaudio_python"])

    return [native]


fill_template()
build_gradle()
copy_source()

setup(
    name='kaudio',
    version='1.0',
    description='Python wrapper for kaudio',
    ext_modules=extensions(),
    packages=['kaudio/'],
)
