from PySide2.QtCore import QCoreApplication, Qt
from kaudio.app.main_app import MainApp


def main():
    QCoreApplication.setAttribute(Qt.AA_EnableHighDpiScaling)
    app = MainApp()
    app.run()


if __name__ == "__main__":
    main()
