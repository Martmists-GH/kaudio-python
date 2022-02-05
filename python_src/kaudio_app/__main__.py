from PySide2.QtCore import QCoreApplication, Qt

from kaudio_app.app import App


def main():
    QCoreApplication.setAttribute(Qt.AA_EnableHighDpiScaling)
    app = App()
    app.run()


if __name__ == "__main__":
    main()
