"""Claude tool definitions + handlers for the agent loop."""
import json

from . import store

TOOLS = [
    {
        "name": "remember_fact",
        "description": "Store a lasting fact about the user (preference, relationship, decision, biography detail). Use whenever the user reveals something worth remembering long-term.",
        "input_schema": {
            "type": "object",
            "properties": {"fact": {"type": "string", "description": "One atomic fact, self-contained."}},
            "required": ["fact"],
        },
    },
    {
        "name": "update_persona_section",
        "description": "Rewrite one section of the TWIN.md persona document (Identity, Voice & Style, Values & Boundaries, Business Context, Preferences). Provide the FULL new body for that section.",
        "input_schema": {
            "type": "object",
            "properties": {
                "section": {"type": "string"},
                "content": {"type": "string"},
            },
            "required": ["section", "content"],
        },
    },
    {
        "name": "write_project_note",
        "description": "Append a dated note to a named project's memory file (status, decision, next step).",
        "input_schema": {
            "type": "object",
            "properties": {
                "project": {"type": "string"},
                "note": {"type": "string"},
            },
            "required": ["project", "note"],
        },
    },
    {
        "name": "search_memory",
        "description": "Keyword-search all memory files (facts, projects, daily summaries, persona) for relevant context not already in the prompt.",
        "input_schema": {
            "type": "object",
            "properties": {"query": {"type": "string"}},
            "required": ["query"],
        },
    },
]


def handle(name: str, args: dict) -> str:
    if name == "remember_fact":
        store.append_fact(args["fact"])
        return "remembered"
    if name == "update_persona_section":
        store.update_persona_section(args["section"], args["content"])
        return f"persona section '{args['section']}' updated"
    if name == "write_project_note":
        store.write_project_note(args["project"], args["note"])
        return f"note added to project '{args['project']}'"
    if name == "search_memory":
        hits = store.search(args["query"])
        if not hits:
            return "no results"
        return json.dumps(hits, ensure_ascii=False)
    return f"unknown tool: {name}"
