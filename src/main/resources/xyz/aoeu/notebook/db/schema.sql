CREATE TABLE IF NOT EXISTS notes (
  id int(11) AUTO_INCREMENT PRIMARY KEY,
  owner BINARY(16),
  contents TEXT,
);

CREATE INDEX IF NOT EXISTS notes_owner_idx ON notes (owner);
