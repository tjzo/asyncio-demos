from fastapi import FastAPI
import asyncio
import uvicorn

app = FastAPI()


@app.get("/ping")
async def ping():
    await asyncio.sleep(5)
    return {"message": "Hello World"}


if __name__ == "__main__":
    uvicorn.run("main:app", host="127.0.0.1", port=5100, log_level="info")