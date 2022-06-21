from os.path import dirname, abspath
from platform import system
from setuptools import setup, Extension, find_packages
from subprocess import Popen, PIPE

osname = system()
debug = False

dir_name = dirname(abspath(__file__))

if osname == "Linux" or osname == "Darwin":
    gradle_bin = "./gradlew"
else:
    gradle_bin = ".\\gradlew.bat"

# Build the project
for i in range(10):
    proc = Popen([gradle_bin, "build"])
    if proc.wait() == 0:
        break
else:
    raise RuntimeError("Build failed")

# Fetch configuration from gradle task
proc = Popen([gradle_bin, "setupMetadata"], stdout=PIPE)
if proc.wait() != 0:
    raise RuntimeError("Fetching metadata failed")
output = proc.stdout.read().decode()
real_output = output.split("===METADATA START===")[1].split("===METADATA END===")[0]

# Apply the configuration
exec(real_output, globals(), locals())
# props added:
has_stubs: bool
project_name: str
project_version: str
build_dir: str
root_dir: str
target: str

print(dir_name)
build_dir = build_dir[len(dir_name)+1:]
root_dir = root_dir[len(dir_name)+1:]


def snake_case(name):
    return name.replace("-", "_").lower()


def extensions():
    folder = "debugStatic" if debug else "releaseStatic"
    prefix = "_" if has_stubs else ""
    native = Extension(prefix + snake_case(project_name),
                       sources=[f"{build_dir}/generated/ksp/{target}/{target}Main/resources/entrypoint.cpp"],
                       # Temporary workaround for [KT-52303](https://youtrack.jetbrains.com/issue/KT-52303)
                       # When resolved, use `{build_dir}/` instead of `build/`
                       include_dirs=[f"build/bin/{target}/{folder}/"],
                       library_dirs=[f"build/bin/{target}/{folder}/"],
                       libraries=[snake_case(project_name)])

    return [native]


with open("README.md", "r") as fp:
    long_description = fp.read()


with open("requirements.txt", "r") as fp:
    requirements = fp.read()


attrs = {}

if has_stubs:
    stub_root = f"{build_dir}/generated/ksp/{target}/{target}Main/resources/"
    attrs["packages"] = find_packages(where=stub_root) + ["kaudio.app"] + ["kaudio.app." + it for it in find_packages(where=f"src/main/kaudio/app")]
    attrs["package_dir"] = {"": stub_root, "kaudio.app": f"src/main/kaudio/app"}
else:
    attrs["packages"] =  ["kaudio.app"] + ["kaudio.app." + it for it in find_packages(where=f"src/main/kaudio/app")]
    attrs["package_dir"] = {"kaudio.app": f"src/main/kaudio/app"}

print(attrs)

setup(
    name=snake_case(project_name),
    version=project_version,
    description=long_description,
    author="Martmists",
    author_email="martmists@gmail.com",
    python_requires=">=3.10",
    ext_modules=extensions(),
    data_files=[
        ("share/applications", [f"src/main/resources/com.martmists.kaudio.desktop"]),
    ],
    entry_points={
        "console_scripts": ["kaudio=kaudio.app.__main__:main"],
        "kaudio.app.nodes": ["builtins=kaudio.app.metadata:load_builtins"]
    },
    extras_require={
        "app": [*filter(lambda req: req != "" and not req.startswith("#"), requirements.split("\n"))],
    },
    classifiers=[
        "Development Status :: 5 - Production/Stable",
        "Environment :: X11 Applications :: Qt",
        "Programming Language :: Kotlin",
        "Programming Language :: Python :: 3.10",
        "Programming Language :: Python :: Implementation :: CPython",
        "Topic :: Multimedia :: Sound/Audio :: Mixers",
        "Intended Audience :: End Users/Desktop",
        "Intended Audience :: Developers",
        "License :: OSI Approved :: BSD License",
    ],
    **attrs
)
