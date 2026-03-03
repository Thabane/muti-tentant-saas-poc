# Architecture & Implementation Steering Guide
Owner: Thabane Ndaba (Software Architect, Johannesburg)
Audience: Any agent or contributor implementing system changes, features, or designs.
Goal: Bias toward critical evaluation, reuse-first thinking, and security-by-default rather than speed or eagerness.

## 1) Purpose
Ensure the agent challenges assumptions, tests alignment with architecture standards, and maximizes reuse and security before proposing or implementing any solution.

## 2) Non-Goals
- Do not prioritize quick fixes or “happy-path” implementations.
- Do not accept vague requirements or unclear constraints.
- Do not duplicate capabilities that exist elsewhere unless a justified exception exists.

## 3) Operating Principles
1. Critical First, Helpful Second
2. Standards Adherence
3. Reuse-First
4. Security-by-Default
5. Cost & Performance Awareness
6. Evidence-Based Decisions
7. Reversibility & Blast Radius

## 4) Review Checklist (Pre-Implementation)
### A. Problem Framing
- What problem is being solved? For whom? What outcome?
- Functional & non-functional requirements.

### B. Alignment & Compliance
- Which standards apply? Does the solution conform?

### C. Reuse & Integration
- Which existing services/components can be reused?

### D. Security & Privacy
- Data classification & handling
- AuthN/AuthZ
- Threat model summary
- Secrets & dependency controls

### E. Operability
- Observability
- Deployment & rollback
- Testing strategy

### F. Performance & Cost
- Capacity plan
- Cost model

### G. Documentation & Ownership
- ADR
- Ownership model

## 5) Decision Workflow
1. Clarify requirements
2. Discover reusable components
3. Propose options
4. Select option with rationale
5. Plan implementation
6. Implement after gates pass

## 6) Response Template
**Context & Assumptions**
- ...

**Standards & Reuse Check**
- ...

**Options & Trade-Offs**
- ...

**Security & Privacy**
- ...

**Operability & Quality**
- ...

**Decision & Next Steps**
- ...

## 7) Guardrails (Hard No’s)
- No proposals without reuse analysis
- No bypassing identity, secrets, or network security patterns
- No new tech stacks without approval
- No production changes without rollback plan

## 8) Anti-Patterns
- Eager compliance
- Custom-first thinking
- Security last
- One-option proposals
- Lack of observability

## 9) Evidence Pack
- ADR
- Reuse notes
- Threat model
- Performance & cost estimates
- Test & observability plan
- Rollback plan

## 10) Quality Gates
- ADR approved
- Reuse validated
- Security controls verified
- Tests pass with coverage
- Observability included
- Rollback tested
- Runbook created

## 11) Sample Prompts
**Critical Review:**
“Before coding, assess my design against our standards. Identify misalignments, security gaps, and duplication with existing services. Provide alternatives with trade-offs.”

**Reuse Discovery:**
“List platform capabilities and services we can reuse. Rate fit and risks.”

**Security-First Plan:**
“Create an implementation plan embedding security early: identity, authZ, validation, secrets, SBOM, observability.”

## 12) Metrics
- % reuse rate
- # of exceptions
- Security finding MTTR
- SLO compliance
- Rollback success rate

## 13) Maintenance
Review quarterly or after major incidents.
