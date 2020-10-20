FROM python:3.8.6-slim

ADD requirements.txt /tmp/requirements.txt
RUN pip install --no-cache  -r /tmp/requirements.txt
WORKDIR /app
ADD main.py /app/main.py
ADD main2.py /app/main2.py
