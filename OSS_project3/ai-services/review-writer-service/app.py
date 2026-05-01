from __future__ import annotations

import os

import httpx
from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI(title="ai-review-writer-service")

FALLBACK_REVIEW_TEXT = "음식이 깔끔하고 맛의 균형이 좋아서 만족스러웠습니다. 다음에도 다시 방문하고 싶은 식당입니다."


class ReviewDraftRequest(BaseModel):
    menuName: str
    keywords: list[str] = []
    rating: int = 5


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
                    "You generate short Korean restaurant reviews. "
                    "Requirements: positive tone, natural Korean, one paragraph, "
                    "rating between 1 and 5 integer."
                ),
            },
            {"role": "user", "content": prompt},
        ],
    }
    headers = {"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"}
    try:
        async with httpx.AsyncClient(timeout=20.0) as client:
            response = await client.post("https://api.openai.com/v1/chat/completions", headers=headers, json=payload)
            response.raise_for_status()
            data = response.json()
        return data["choices"][0]["message"]["content"]
    except Exception:
        return None


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/write-review")
async def write_review(request: ReviewDraftRequest) -> dict[str, str | int]:
    prompt = (
        "다음 조건으로 후기를 작성해줘.\n"
        "- 긍정적인 평가를 내려주면 좋다.\n"
        "- 별점은 1~5의 정수.\n"
        "- 2문장 이상 4문장 이하.\n"
        "- 메뉴명과 키워드를 자연스럽게 포함.\n\n"
        f"메뉴: {request.menuName}\n"
        f"키워드: {', '.join(request.keywords)}\n"
        f"요청 별점: {request.rating}"
    )
    content = await call_openai(prompt)
    if not content:
        content = FALLBACK_REVIEW_TEXT
    safe_rating = max(1, min(5, int(request.rating)))
    return {"content": content, "rating": safe_rating}

