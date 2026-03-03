
---
inclusion: always
---

# Documentation Standards (Markdown)

This project uses strict, consistent Markdown formatting so that Kiro can reliably generate, edit, and lint `.md` files on Windows environments.

## Line Endings
- Always write Markdown files using **LF (`\n`) line endings**, even on Windows.
- Do not insert CRLF or mixed line endings.
- When writing files, ensure consistency with repository `.gitattributes` rules (LF normalized).  
  Kiro should avoid inserting extra newlines or CRLF artifacts.  

## Required Formatting Rules
- No trailing spaces at end of lines (avoids MD009, MD012).  
- Limit line length to 120 characters.  
- Use ATX (`#`) headings only; don’t use Setext headings.  
- Blank lines:
  - One blank line before/after headings.
  - Do **not** add extra blank lines automatically (past versions of Kiro added extra newlines in `task.md`).  

## Markdownlint Alignment
Ensure Kiro conforms to these rules during generation:
- MD009 — no trailing spaces  
- MD012 — no multiple consecutive blank lines  
- MD013 — max line length (120)  
- MD022 — proper heading spacing  
- MD031 — no blank lines inside fenced blocks  
- MD041 — first line must be a heading  

## File Output Requirements
- When writing `.md` files, Kiro should:
  - Write the **full document** to disk in one operation.
  - Avoid partial writes or “0‑byte” files (a known Windows file‑sync issue).  
  - Confirm that the file exists and has non‑zero length after generation.

## Code Blocks
- Always fence code with triple backticks.
- Explicitly specify language when known:  
  Example:  
  ```ts
  console.log("hello");
 
