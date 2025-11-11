CREATE TABLE IF NOT EXISTS scripts (
  id      INTEGER PRIMARY KEY AUTOINCREMENT,
  name    TEXT NOT NULL UNIQUE,
  group_name   TEXT NOT NULL,
  hash    TEXT,
  next_run TEXT NOT NULL,
  enabled INTEGER NOT NULL DEFAULT 1,
  compile_failed INTEGER NOT NULL DEFAULT 0,
  enqueued INTEGER NOT NULL DEFAULT 1,
  run_count INTEGER NOT NULL DEFAULT 0,
  fail_count INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS results (
  id               INTEGER PRIMARY KEY AUTOINCREMENT,
  script_id        INTEGER NOT NULL,
  status           TEXT NOT NULL,
  timestamp        TEXT NOT NULL,
  result_hash      TEXT DEFAULT NULL,
  delivered        INTEGER NOT NULL DEFAULT 0,
  FOREIGN KEY (script_id) REFERENCES scripts(id) ON DELETE CASCADE
);
