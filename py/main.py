from fastapi import FastAPI
import asyncio
import time
import uvicorn
import os
import httpx

app = FastAPI()

REMOTE_ADDR = os.getenv("REMOTE_ADDR", "http://127.0.0.1:5100/worker")


async def download(idx: int):
    async with httpx.AsyncClient() as cli:
        r = await cli.get(REMOTE_ADDR, timeout=20)
    return r.json()["message"]


@app.get("/worker")
async def worker():
    await asyncio.sleep(5)
    return {"message": "Hello World"}


@app.get("/manager")
async def manager():
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
    uvicorn.run("main:app", host="127.0.0.1", port=5100, log_level="info")
