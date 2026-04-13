from __future__ import annotations

import json
import os
from typing import Any

import httpx
from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI(title="ai-recommendation-service")

FALLBACK_MENU_MESSAGE = (
    "\uc9c0\uae08\uc740 AI \ucd94\ucc9c \uc124\uba85\uc744 \uc790\uc138\ud788 \ub9cc\ub4e4 \uc218 \uc5c6\uc5b4\uc694. "
    "\ub300\uc2e0 \ud604\uc7ac \uc785\ub825\uacfc \uac00\uc7a5 \uc798 \ub9de\ub294 \uba54\ub274 3\uac00\uc9c0\ub97c \uace8\ub77c \ucd94\ucc9c\ud574\ub4dc\ub838\uc5b4\uc694."
)

KEYWORD_TOKENS = [
    "\ub9e4\ucf64",
    "\ub9e4\uc6b4",
    "\ub4e0\ub4e0",
    "\uac00\ubcbc\uc6b4",
    "\uac00\ubccd",
    "\ucee4\ud53c",
    "\ubd80\ub4dc\ub7ec\uc6b4",
    "\ubd80\ub4dc\ub7fd",
    "\ubc14\uc0ad",
    "\ucc44\uc18c",
    "\ub530\ub73b",
    "\uc2dc\uc6d0",
    "\uac74\uac15",
    "\ud06c\ub9bc",
    "\uace0\uae30",
    "\uad6d\ubb3c",
]


class RecommendRequest(BaseModel):
    message: str
    menus: list[dict[str, Any]]


async def call_openai(prompt: str) -> str | None:
    api_key = os.getenv("OPENAI_API_KEY", "").strip()
    model = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
    if not api_key:
        return None

    payload = {
        "model": model,
        "messages": [
            {
                "role": "system",
                "content": (
                    "You are a menu recommendation engine for a restaurant. "
                    "Always answer in Korean. Give concise and actionable reasoning. "
                    "Avoid hallucinating menu names not in candidates."
                ),
            },
            {"role": "user", "content": prompt},
        ],
    }
    headers = {"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"}

    try:
        async with httpx.AsyncClient(timeout=20.0) as client:
            response = await client.post(
                "https://api.openai.com/v1/chat/completions",
                headers=headers,
                json=payload,
            )
            response.raise_for_status()
            data = response.json()
        return data["choices"][0]["message"]["content"]
    except Exception:
        return None


def score_menu(user_message: str, menu: dict[str, Any]) -> int:
    score = 0
    haystack = f"{menu.get('name', '')} {menu.get('description', '')} {menu.get('keywords', '')}".lower()

    for token in KEYWORD_TOKENS:
        if token in user_message and token in haystack:
            score += 3

    for word in user_message.split():
        cleaned = word.strip(",.!? ")
        if cleaned and cleaned in haystack:
            score += 1

    return score


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/recommend-menu")
async def recommend_menu(request: RecommendRequest) -> dict[str, Any]:
    user_message = request.message.lower()
    scored: list[tuple[int, dict[str, Any]]] = []

    for menu in request.menus:
        scored.append((score_menu(user_message, menu), menu))

    scored.sort(key=lambda item: (-item[0], item[1].get("price", 0)))
    picks = [item[1] for item in scored[:3]]

    prompt = (
        "\uc544\ub798\ub294 \uc0ac\uc6a9\uc790 \uc694\uccad\uacfc \ud6c4\ubcf4 \uba54\ub274 \ubaa9\ub85d\uc785\ub2c8\ub2e4.\n"
        "1. \ud6c4\ubcf4 \uc911 \ucd5c\ub300 3\uac1c\ub97c \uc6b0\uc120\uc21c\uc704\ub300\ub85c \ucd94\ucc9c\ud558\uc138\uc694.\n"
        "2. \ucd94\ucc9c \uc774\uc720\ub294 2~3\ubb38\uc7a5\uc73c\ub85c \uc124\uba85\ud558\uc138\uc694.\n"
        "3. \ucd94\ucc9c \uba54\ub274\uba85\uc740 \ubc18\ub4dc\uc2dc \ud6c4\ubcf4 \ubaa9\ub85d\uc5d0 \uc788\ub294 \uc774\ub984\ub9cc \uc0ac\uc6a9\ud558\uc138\uc694.\n\n"
        f"\uc0ac\uc6a9\uc790 \uc694\uccad: {request.message}\n"
        f"\ud6c4\ubcf4 \uba54\ub274(JSON): {json.dumps(request.menus, ensure_ascii=False)}"
    )

    explanation = await call_openai(prompt)
    if not explanation:
        explanation = FALLBACK_MENU_MESSAGE

    return {"recommendedMenus": picks, "message": explanation}
