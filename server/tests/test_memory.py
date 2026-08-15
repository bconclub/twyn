import importlib
import sys


def setup_env(tmp_path, monkeypatch):
    monkeypatch.setenv("TWIN_DATA_DIR", str(tmp_path))
    monkeypatch.setenv("TWIN_TOKEN", "test-token")
    for mod in ["twin_server.config", "twin_server.db", "twin_server.memory.store"]:
        if mod in sys.modules:
            importlib.reload(sys.modules[mod])
    from twin_server.memory import store
    return store


def test_fact_roundtrip_and_search(tmp_path, monkeypatch):
    store = setup_env(tmp_path, monkeypatch)
    store.append_fact("prefers espresso over filter coffee")
    assert "espresso" in store.read(store.FACTS_FILE)
    hits = store.search("espresso")
    assert hits and any("espresso" in h["content"] for h in hits)


def test_persona_section_update(tmp_path, monkeypatch):
    store = setup_env(tmp_path, monkeypatch)
    store.ensure_persona()
    store.update_persona_section("Identity", "Builder. Based in nowhere. Ships fast.")
    persona = store.read(store.PERSONA_FILE)
    assert "Ships fast." in persona
    assert persona.count("## Identity") == 1
    # update replaces, not appends
    store.update_persona_section("Identity", "Changed body.")
    persona = store.read(store.PERSONA_FILE)
    assert "Changed body." in persona
    assert "Ships fast." not in persona


def test_persona_new_section_appended(tmp_path, monkeypatch):
    store = setup_env(tmp_path, monkeypatch)
    store.ensure_persona()
    store.update_persona_section("Health", "Sleeps late.")
    assert "## Health" in store.read(store.PERSONA_FILE)


def test_project_note(tmp_path, monkeypatch):
    store = setup_env(tmp_path, monkeypatch)
    store.write_project_note("Project TWIN", "v0 scaffolded")
    content = store.read("projects/project-twin.md")
    assert "v0 scaffolded" in content


def test_path_escape_blocked(tmp_path, monkeypatch):
    store = setup_env(tmp_path, monkeypatch)
    import pytest
    with pytest.raises(ValueError):
        store.read("../../etc/passwd")


def test_reindex_survives_restart(tmp_path, monkeypatch):
    store = setup_env(tmp_path, monkeypatch)
    store.append_fact("the sideproject codename is falcon")
    # simulate restart: wipe index, rebuild from files
    from twin_server import db
    db.get_conn().execute("DELETE FROM memory_fts")
    db.get_conn().commit()
    assert store.search("falcon") == []
    store.reindex_all()
    assert any("falcon" in h["content"] for h in store.search("falcon"))
