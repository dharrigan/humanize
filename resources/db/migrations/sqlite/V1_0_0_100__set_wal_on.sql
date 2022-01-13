--
-- Set SQLITE to use Write Ahead Log (WAL).
--
-- This setting is persistent across restarts.
--
-- https://sqlite.org/wal.html
--

PRAGMA journal_mode=WAL;

--
-- END
--
