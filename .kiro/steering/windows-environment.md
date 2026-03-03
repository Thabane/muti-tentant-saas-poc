```markdown
---
inclusion: always
---

# Windows Environment Stability Rules

Kiro must compensate for Windows‑specific quirks in file writing, path handling, and Markdown generation.

## File System Behaviour
Windows may fail to flush file writes, resulting in empty (`0 bytes`) files.  
Kiro must:

- Use **atomic writes** (write temp file → rename).  
- Re‑check file size after writing.  
- Avoid incremental writes unless necessary.  

## Path Handling
- Always quote paths containing spaces.  
- Escape paths correctly when using Kiro commands or generating scripts.  

## CRLF/LF Consistency on Windows
Because Git may rewrite line endings depending on `core.autocrlf`, Kiro must:

- Write Markdown using **LF**.
- Never introduce CRLF mid‑file.
- Be aware that Git may rewrite endings, causing apparent diffs.  
