from PySide2 import QtCore
from kaudio_app.app import App


def main():
    QtCore.QCoreApplication.setAttribute(QtCore.Qt.AA_EnableHighDpiScaling)
    app = App()
    app.run()


if __name__ == "__main__":
    main()
