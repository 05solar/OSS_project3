from __future__ import annotations

import json
import os
from typing import Any

import httpx
from fastapi import FastAPI

app = FastAPI(title="ai-operations-service")

FALLBACK_NEW_MENU_TEXT = "신메뉴 추천: 매콤 차돌된장리조또, 유자 바질 냉파스타, 흑임자 크림라떼"
FALLBACK_QUALITY_TEXT = "서비스 품질 기본 점검 결과: 응대 속도 개선, 피크타임 좌석 회전 안내, 메뉴 설명 통일이 필요합니다."


async def call_openai(system_prompt: str, user_prompt: str) -> str | None:
    api_key = os.getenv("OPENAI_API_KEY", "").strip()
    model = os.getenv("OPENAI_MODEL", "gpt-4o-mini")
    if not api_key:
        return None
    payload = {
        "model": model,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt},
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


@app.post("/analyze-traffic")
def analyze_traffic(payload: dict[str, Any]) -> dict[str, Any]:
    summary = payload.get("salesSummary", {}) if isinstance(payload, dict) else {}
    pending = int(summary.get("pendingOrders", 0))
    total_orders = int(summary.get("totalOrders", 0))
    level = "여유"
    wait_minutes = 10
    if pending >= 5 or total_orders >= 15:
        level = "혼잡"
        wait_minutes = 25
    elif pending >= 2:
        level = "보통"
        wait_minutes = 16
    return {
        "trafficLevel": level,
        "estimatedWaitMinutes": wait_minutes,
        "message": f"현재 매장 상태는 {level} 수준이며 예상 대기 시간은 약 {wait_minutes}분입니다.",
    }


@app.post("/suggest-new-menu")
async def suggest_new_menu(payload: dict[str, Any]) -> dict[str, str]:
    histories = payload.get("orders", [])
    result = await call_openai(
        system_prompt=(
            "You are a restaurant R&D assistant. "
            "Return concise Korean suggestions based on order trends."
        ),
        user_prompt=(
            "주문 이력을 분석해 신메뉴 3개를 제안해줘.\n"
            "조건: 기존 인기 카테고리를 반영하고, 계절성 1개 포함.\n"
            "형식: 번호 목록 3줄.\n"
            f"주문 이력(JSON): {json.dumps(histories, ensure_ascii=False)}"
        ),
    )
    if not result:
        result = FALLBACK_NEW_MENU_TEXT
    return {"suggestion": result}


@app.post("/evaluate-service-quality")
async def evaluate_service_quality(payload: dict[str, Any]) -> dict[str, str]:
    reviews = payload.get("reviews", [])
    result = await call_openai(
        system_prompt=(
            "You are a quality analyst for restaurant operations. "
            "Answer in Korean with practical actions."
        ),
        user_prompt=(
            "후기 데이터를 바탕으로 서비스 품질을 평가해줘.\n"
            "조건: 100점 만점 점수 1개, 개선 항목 2개, 장점 1개.\n"
            "형식: 짧은 단락 3개 이내.\n"
            f"후기(JSON): {json.dumps(reviews, ensure_ascii=False)}"
        ),
    )
    if not result:
        result = FALLBACK_QUALITY_TEXT
    return {"analysis": result}
