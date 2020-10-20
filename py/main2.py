from fastapi import FastAPI
import asyncio
import time
import uvicorn
import os
import httpx

app = FastAPI()

REMOTE_ADDR = os.getenv("REMOTE_ADDR", "http://127.0.0.1:5100/ping")


async def download(idx: int):
    async with httpx.AsyncClient() as cli:
        r = await cli.get(REMOTE_ADDR, timeout=20)
    return r.json()["message"]


@app.get("/ping")
async def ping():
    before = time.time()
    res = await asyncio.gather(*[download(i) for i in range(100)])
    time_used = time.time() - before
    return {
        "message": "Hello World",
        "res_len": len(res),
        "res5": res[:5],
        "time_used": time_used
    }


if __name__ == "__main__":
    uvicorn.run("main2:app", host="127.0.0.1", port=5101, log_level="info")
