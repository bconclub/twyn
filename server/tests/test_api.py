import importlib
import sys

from fastapi.testclient import TestClient


def make_client(tmp_path, monkeypatch):
    monkeypatch.setenv("TWIN_DATA_DIR", str(tmp_path))
    monkeypatch.setenv("TWIN_TOKEN", "test-token")
    monkeypatch.setenv("ANTHROPIC_API_KEY", "unused-in-tests")
    for mod in list(sys.modules):
        if mod.startswith("twin_server"):
            importlib.reload(sys.modules[mod])
    from twin_server.main import app
    return TestClient(app)


def test_health_open(tmp_path, monkeypatch):
    c = make_client(tmp_path, monkeypatch)
    assert c.get("/health").json() == {"ok": True}


def test_auth_required(tmp_path, monkeypatch):
    c = make_client(tmp_path, monkeypatch)
    assert c.get("/messages").status_code == 401
    assert c.get("/messages", headers={"Authorization": "Bearer wrong"}).status_code == 401
    assert c.get("/messages", headers={"Authorization": "Bearer test-token"}).status_code == 200


def test_memory_endpoints(tmp_path, monkeypatch):
    c = make_client(tmp_path, monkeypatch)
    h = {"Authorization": "Bearer test-token"}
    r = c.put("/memory/projects/test.md", json={"content": "# Test\nhello"}, headers=h)
    assert r.status_code == 200
    assert "hello" in c.get("/memory/projects/test.md", headers=h).text
    assert c.get("/memory/nope.md", headers=h).status_code == 404
    assert c.put("/memory/evil.txt", json={"content": "x"}, headers=h).status_code == 400
