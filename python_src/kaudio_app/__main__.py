from PySide2.QtCore import QCoreApplication, Qt
from PySide2.QtWidgets import QApplication

from kaudio_app.app import App


def main():
    QCoreApplication.setAttribute(Qt.AA_EnableHighDpiScaling)
    # QApplication.setHighDpiScaleFactorRoundingPolicy(Qt.HighDpiScaleFactorRoundingPolicy.PassThrough)
    app = App()
    app.run()


if __name__ == "__main__":
    main()
