import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))
import public_validate

def main():
    cases_file = ROOT / "tests" / "my_cases.jsonl"
    expected_file = ROOT / "tests" / "my_expected.jsonl"
    actual_file = ROOT / "tests" / "my_actual.jsonl"

    inputs = [json.loads(line) for line in cases_file.read_text(encoding="utf-8").splitlines() if line.strip()]
    expected = [json.loads(line) for line in expected_file.read_text(encoding="utf-8").splitlines() if line.strip()]
    actual = [json.loads(line) for line in actual_file.read_text(encoding="utf-8").splitlines() if line.strip()]

    for i in range(len(inputs)):
        if not public_validate.check(inputs[i], actual[i], expected[i]):
            print(f"Case {i + 1} failed")
            sys.exit(1)
        print(f"Case {i + 1} ({inputs[i]['task']}): OK")

    # Check alternative valid answers and invalid witnesses
    req_mst, exp_mst = inputs[2], expected[2]
    assert public_validate.check(req_mst, {"cost": -12, "edge_ids": [2, 4, 6], "components": 3}, exp_mst)
    assert not public_validate.check(req_mst, {"cost": -12, "edge_ids": [0, 4, 5], "components": 3}, exp_mst)

    req_scc, exp_scc = inputs[6], expected[6]
    assert public_validate.check(req_scc, {"component": [10, 10, 10, 20, 20, 20, 5], "condensation": [[10, 20]]}, exp_scc)
    assert not public_validate.check(req_scc, {"component": [10, 10, 10, 20, 20, 20, 5], "condensation": [[10, 20], [10, 20]]}, exp_scc)

    req_topo, exp_topo = inputs[7], expected[7]
    assert public_validate.check(req_topo, {"acyclic": True, "order": [0, 1, 2, 3, 4]}, exp_topo)
    assert not public_validate.check(req_topo, {"acyclic": True, "order": [0, 3, 1, 2, 4]}, exp_topo)

    req_dag, exp_dag = inputs[9], expected[9]
    assert public_validate.check(req_dag, {"acyclic": True, "distance": [None, 0, 4, 2, -2, None], "parent_edge": [-1, -1, 3, 2, 5, -1]}, exp_dag)
    assert not public_validate.check(req_dag, {"acyclic": True, "distance": [None, 0, 4, 2, -2, None], "parent_edge": [-1, -1, 0, 2, 5, -1]}, exp_dag)
    print("Alternative optima and invalid witness checks: OK")

if __name__ == "__main__":
    main()
