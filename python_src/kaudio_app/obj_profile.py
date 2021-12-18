import sys
import objgraph


def profile_growth(name: str):
    result = objgraph.growth(10, shortnames=True, filter=None)

    if result:
        print(f"Growth after {name}:")
        width = max(len(name) for name, _, _ in result)
        for name, count, delta in result:
            sys.stdout.write('| %-*s%9d %+9d\n' % (width, name, count, delta))
