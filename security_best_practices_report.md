# Security Best Practices Report

## Executive Summary

This repository is a Java 11 Maven desktop plugin for Jameica/Hibiscus, not one of the `security-best-practices` skill's first-class stacks. I therefore performed a best-effort review against general Java desktop, file-handling, and data-protection practices.

I found three material security issues:

1. Untrusted filenames are written directly beneath the temporary directory when opening attachments/documents, which enables path traversal and arbitrary file overwrite within the current user's permissions.
2. The embedded H2 database and a created `readonly` account both use the static password `jverein`, which removes any meaningful credential boundary for local or network-exposed database access.
3. Lesefeld scripts are edited in the UI and executed through BeanShell without sandboxing, which makes imported/shared database content a code-execution boundary.

## Critical Findings

### SBP-001: Path traversal and arbitrary file overwrite via temporary document extraction

**Severity:** Critical

**Impact:** A crafted attachment or document filename can cause JVerein to overwrite arbitrary files writable by the current user when the file is opened from the UI.

**Evidence**

- [src/de/jost_net/JVerein/gui/action/MailAnhangAnzeigeAction.java](/Users/tobias/code/jverein/src/de/jost_net/JVerein/gui/action/MailAnhangAnzeigeAction.java#L44) creates a file with `new File(System.getProperty("java.io.tmpdir"), ma.getDateiname())` and writes bytes to it at lines 45-50.
- [src/de/jost_net/JVerein/gui/action/DokumentShowAction.java](/Users/tobias/code/jverein/src/de/jost_net/JVerein/gui/action/DokumentShowAction.java#L52) creates a file with `System.getProperty("java.io.tmpdir") + "/" + map.get("filename")` and writes bytes to it at lines 62-67.

**Why this is a problem**

Both code paths trust filenames originating from stored content. If `ma.getDateiname()` or `map.get("filename")` contains path separators such as `../`, absolute paths, or platform-specific traversal forms, Java will resolve them as filesystem paths rather than harmless leaf names. On a desktop app that routinely handles user-imported and synced content, this is a direct arbitrary-write primitive.

**Recommended fix**

- Strip all directory components and keep only a safe basename before writing.
- Prefer `File.createTempFile(...)` or `Files.createTempFile(...)` and append only a sanitized extension.
- Reject or normalize filenames containing separators, control characters, or reserved names.
- Set restrictive file permissions for extracted temp files where supported.

## High Findings

### SBP-002: Hardcoded H2 database credentials

**Severity:** High

**Evidence**

- [src/de/jost_net/JVerein/server/DBSupportH2Impl.java](/Users/tobias/code/jverein/src/de/jost_net/JVerein/server/DBSupportH2Impl.java#L78) returns the hardcoded password `jverein` at lines 79-82.
- [src/de/jost_net/JVerein/server/DDLTool/Updates/Update0418.java](/Users/tobias/code/jverein/src/de/jost_net/JVerein/server/DDLTool/Updates/Update0418.java#L29) creates a `readonly` H2 user with password `jverein` at lines 30-35.

**Why this is a problem**

The code removes the protective value of database authentication for H2 deployments. If `AUTO_SERVER=TRUE` is enabled or the DB files are reachable from another local process/user context, the credentials are predictable and shared across installations. Even without remote exposure, this weakens incident containment and makes data exfiltration materially easier.

**Recommended fix**

- Generate a per-installation random database password on first run.
- Store it encrypted via the platform keystore or the host application's secure secret storage.
- Remove the static `readonly` password, or disable the account unless it is explicitly configured by the operator.
- If compatibility requires a migration path, rotate existing known credentials during startup or upgrade.

### SBP-003: Unsandboxed BeanShell execution from database-backed Lesefeld scripts

**Severity:** High

**Evidence**

- [src/de/jost_net/JVerein/util/LesefeldAuswerter.java](/Users/tobias/code/jverein/src/de/jost_net/JVerein/util/LesefeldAuswerter.java#L70) instantiates a raw BeanShell `Interpreter` at lines 70-73.
- [src/de/jost_net/JVerein/util/LesefeldAuswerter.java](/Users/tobias/code/jverein/src/de/jost_net/JVerein/util/LesefeldAuswerter.java#L206) evaluates arbitrary script text via `bsh.eval(script)` at lines 206-208.
- [src/de/jost_net/JVerein/util/LesefeldAuswerter.java](/Users/tobias/code/jverein/src/de/jost_net/JVerein/util/LesefeldAuswerter.java#L222) pulls the script from persisted `Lesefeld` objects and executes it at lines 224-229.
- [src/de/jost_net/JVerein/gui/view/LesefeldDetailView.java](/Users/tobias/code/jverein/src/de/jost_net/JVerein/gui/view/LesefeldDetailView.java#L42) exposes "Skript-Code" as an editable UI field at lines 42-48.
- [pom.xml](/Users/tobias/code/jverein/pom.xml#L18) declares BeanShell in the build properties at lines 18-20.

**Why this is a problem**

This is effectively a built-in scripting engine with full application privileges and no sandbox. If Lesefeld definitions are restored from backups, imported from other environments, or modified by a lower-trust local actor, opening or evaluating member views becomes arbitrary code execution inside the JVerein process.

**Recommended fix**

- Treat Lesefeld scripts as privileged admin-only content and document that trust boundary explicitly.
- Add an execution allowlist, disable access to dangerous Java classes/packages, or replace BeanShell with a constrained expression language.
- Require explicit confirmation before executing scripts originating from imported/restored data.
- Consider signing trusted script content or storing an origin marker so imported scripts are quarantined until reviewed.

## Notes / Lower-Confidence Risks Not Counted As Findings

- XML import code in [src/de/jost_net/JVerein/io/KontenrahmenImportXML.java](/Users/tobias/code/jverein/src/de/jost_net/JVerein/io/KontenrahmenImportXML.java) and [src/de/jost_net/JVerein/io/KontenrahmenImportXMLv2.java](/Users/tobias/code/jverein/src/de/jost_net/JVerein/io/KontenrahmenImportXMLv2.java) uses NanoXML parsers without visible hardening flags. I did not elevate this to a finding because the parser behavior depends on the library defaults and I did not confirm an exploitable XXE path in this repo.
- CSV import code constructs SQL from the selected filename in [src/de/jost_net/JVerein/io/CSVBuchungsImport.java](/Users/tobias/code/jverein/src/de/jost_net/JVerein/io/CSVBuchungsImport.java) and [src/de/jost_net/JVerein/io/MitgliederImport.java](/Users/tobias/code/jverein/src/de/jost_net/JVerein/io/MitgliederImport.java). This deserves cleanup, but I did not confirm exploitability against the CSV JDBC driver before this report.

## Recommended Remediation Order

1. Fix SBP-001 first because it is a direct arbitrary-write issue with a simple, low-risk remediation.
2. Fix SBP-002 next by eliminating static credentials and rotating existing H2 secrets.
3. Decide on the product stance for SBP-003, then either sandbox or explicitly scope Lesefeld scripting to trusted administrators only.
