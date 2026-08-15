FROM python:3.12-slim

WORKDIR /srv/twin
COPY server/pyproject.toml ./
COPY server/twin_server ./twin_server
RUN pip install --no-cache-dir .

ENV TWIN_DATA_DIR=/srv/twin/data
VOLUME /srv/twin/data
EXPOSE 8000

CMD ["uvicorn", "twin_server.main:app", "--host", "0.0.0.0", "--port", "8000"]
