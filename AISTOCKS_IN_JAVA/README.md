# AISTOCKS_IN_JAVA

**Java 21+ twin** of the [AI Stock Forecaster](../README.md) research system: same **CLI command names**, same **environment variable names** for vendor APIs, the same **100-name AI universe** (bundled as `ai_universe.json`), and the same **placeholder scoring** behavior as the current Python `run_scoring` / `placeholder_top_ranked_summary` path.

> **Which AI stocks are most attractive to buy today (risk-adjusted) over the next 20 / 60 / 90 trading days — and can we trust the model right now?**  
> *(Research framing from the parent project; Java edition is a **porting scaffold** until models and DuckDB are fully replicated.)*

This folder lives **inside** the Python repository as `AISTOCKS_IN_JAVA/`, so one checkout gives you both stacks. The authoritative methodology, frozen evaluation artifacts, and 1,000+ Python tests remain in the parent repo until explicitly ported.

---

## Headline Results (from the Python system — reference only)

| Metric | Value | What It Means |
|--------|------:|---------------|
| Shadow portfolio Sharpe | **2.73** | Vol-sized LGB, 20d monthly L/S, 82.6% hit rate |
| FINAL holdout Sharpe | **1.91** | Survives into 2024 out-of-sample |
| Regime-trust AUROC | **0.72** (0.75 FINAL) | Knows when the model works vs. fails |
| Regime-gated precision | **80%** | When system says "trade," it's right 80% of the time |
| 2024 crisis detection | G(t) → 0 by April | Correctly triggers abstention during regime failure |
| DEUP conformal intervals | **25× better** cond. coverage | Best-in-class calibrated prediction intervals |
| Test coverage | **1,000+** tests | All passing **in Python** |

---

## What the Parent (Python) Project Does

- Generates **cross-sectional rankings** (top buys / neutral / avoid) for a dynamic universe of AI-exposed U.S. equities  
- Produces **expected excess return** vs benchmark (QQQ) at **20 / 60 / 90 trading-day horizons**  
- Provides **calibrated prediction intervals** (P5 / P50 / P95) via DEUP-normalized conformal prediction  
- Estimates **per-stock uncertainty** (ê(x)) and **per-date regime trust** (G(t))  
- Enforces **strict PIT correctness** and **survivorship-safe universe replay**  
- Evaluates with **walk-forward splits**, **DEV/FINAL holdout**, and **cost-realism overlays**

## What the Parent Project Does *Not* Do

- No broker connections, no execution, no live capital management  
- Portfolio construction exists only to test economic meaning after costs and turnover  
- **Research / decision-support**, not trading software  

The **Java** edition inherits the same non-goals: it is **not** a broker or execution layer.

---

## Java Port — Current Scope (read this before trading on anything)

| Area | Python (`src/`) | Java (`com.aistocks.*`) |
|------|-----------------|-------------------------|
| CLI entry | `python -m src.cli …` | `./gradlew run -- …` or `java -jar …` |
| Universe definitions | `universe/ai_stocks.py` | `AiUniverseRegistry` + `ai_universe.json` |
| Universe pipeline (seed path) | `pipelines/universe_pipeline.py` | `UniversePipeline` |
| Scoring (production) | TODO / integration | **Not ported** — placeholder only |
| Placeholder top-N print | `placeholder_top_ranked_summary` | `ScoringPipeline.placeholderTopRankedSummary` (same *math*; rank order may differ from Python because `java.util.Random` ≠ CPython `random`) |
| FMP HTTP client | `data/fmp_client.py` | `FmpClient` (stable base URL + `apikey` query param, disk cache) |
| Polygon / Alpha Vantage | full clients | **Credential resolution only** (keys available to future clients) |
| DuckDB feature store | extensive | **Not ported** |
| LightGBM / Kronos / FinText / FinBERT | Python + torch | **Not ported** (would need JNI, ONNX, DJL, or sidecar) |
| DEUP / conformal / shadow PF | `uncertainty/` | **Not ported** |
| Chapter scripts | `scripts/*.py` | **Not ported** — call Python for batch research |
| Tests | 1,000+ | minimal JUnit (`CredentialsTest`) |

**Bottom line:** this Java project is a **credible shell**: config parity, universe parity, CLI parity, and a **minimal FMP client** for smoke tests. It does **not** yet reproduce research-grade signals.

---

## Architecture Overview (aligned with parent)

```
Data Layer (Ch 3-5)           Signal Layer (Ch 7-11)          Risk Layer (Ch 12-13)
─────────────────────         ───────────────────────         ──────────────────────
FMP fundamentals              LightGBM (primary)              Regime diagnostics
Polygon price data     →      FinText-TSFM (Ch 9)      →     DEUP uncertainty ê(x)
SEC filings/events            NLP Sentiment (Ch 10)           Expert health H(t)/G(t)
DuckDB feature store          Fusion models (Ch 11)           Conformal intervals
Regime context                Vol-sizing (Ch 12)              Binary deployment gate
```

In Java, only the **left column** has a thin, real implementation (FMP). The center and right columns are **documentation mirrors** until ported.

**Two-level uncertainty** (parent system — not yet implemented in Java):

| Level | Signal | Question | Method (Python) |
|-------|--------|----------|-----------------|
| **Per-stock** | ê(x) | Which names are dangerous today? | DEUP error predictor |
| **Per-date** | G(t) | Is the model usable today? | Health gate |

---

## Key Findings (Python — condensed)

### The Model Works — Until It Doesn't

The LightGBM ranker is strong in-sample (2016–2023); 2024 shows regime stress. Only shorter horizons remain comparatively robust in FINAL holdout. See parent `README.md` and `documentation/RESULTS.md`.

### The System Knows When It Fails

DEUP’s **G(t)** gate is designed to abstain in bad regimes (e.g. Mar–Jul 2024 narrative in the parent docs).

### Per-Stock Uncertainty

ê(x) and vol-sizing tradeoffs are discussed in detail in the parent README and Chapter 13 documentation.

---

## Signal Quality (Python metrics — unchanged)

See parent README tables: **All Models — Signal Metrics**, **Shadow Portfolio**, **Holdout Performance**. Java does not recompute these until the evaluation stack is ported.

---

## Project Status

| Chapter | Description | Python | Java |
|---------|-------------|:------:|:----:|
| Ch 1–5 | Data infra, universe, features | ✅ | ⏳ partial (universe + FMP stub) |
| Ch 6–13 | Evaluation through DEUP | ✅ / ⏳ | ⏳ |
| Ch 14–17 | Monitoring, interfaces | ⏳ | ⏳ |

### Chapter 13 (DEUP) Progress

Same subsection table as the parent `README.md`; Java has **no DEUP implementation** yet.

---

## Quick Start (Java)

### 0. Install JDK **21 or newer** (required)

The Gradle wrapper **needs a JVM** on your machine to start. (On this developer machine, no JDK was preinstalled; install one of the following.)

**macOS (recommended — Eclipse Temurin 21 LTS)**

- Download: [Adoptium Temurin 21](https://adoptium.net/)  
- Or (if you use Homebrew): `brew install openjdk@21` then follow the `brew` caveats to add `java` to your `PATH`.

**Verify**

```bash
java -version   # should report 21 or higher
```

**Toolchain note:** `build.gradle.kts` pins **Java 21** via Gradle’s toolchain. With the [Foojay resolver](https://github.com/gradle/foojay-resolve-plugin) in `settings.gradle.kts`, Gradle can **provision** a JDK for compilation **after** the wrapper itself can run.

### 1. Build

```bash
cd AISTOCKS_IN_JAVA
./gradlew build
```

### 2. Configure API keys (same as Python)

Use **identical** names to the parent project:

```properties
FMP_KEYS="your_fmp_api_key_here"
POLYGON_KEYS="your_polygon_api_key_here"
ALPHAVANTAGE_KEYS="your_alphavantage_api_key_here"
SEC_CONTACT_EMAIL="your_email@example.com"
```

**Where to put `.env`**

1. `AISTOCKS_IN_JAVA/.env`, and/or  
2. Parent repo root `.env` (one directory up from `AISTOCKS_IN_JAVA` when nested in this layout) — loaded **only for variables not already set** in the process environment (same as Python `load_dotenv(..., override=False)`).

**Overrides**

- Per-invocation: `aistocks --dotenv /path/to/.env …`  
- Environment: `export AISTOCKS_DOTENV=/path/to/dir/or/file`

**FMP key precedence** (matches `src/utils/env.py` `resolve_fmp_key`):

1. CLI `--api-key` (on `download-data`)  
2. First non-empty entry of comma-separated `FMP_KEYS`  
3. `FMP_API_KEY`

Polygon / Alpha Vantage: first comma-separated token from `POLYGON_KEYS` / `ALPHAVANTAGE_KEYS` via `Credentials` (ready for future HTTP clients).

### 3. Run CLI (Picocli)

Global options **before** the subcommand:

```bash
./gradlew run -- --dotenv ../.env score --asof 2026-03-20
./gradlew run -- list-universe
./gradlew run -- build-universe --asof 2026-03-20 --max-size 100
./gradlew run -- download-data --start 2024-01-01 --end 2024-01-31 --tickers NVDA --dry-run
```

**Fat JAR (optional)**

```bash
./gradlew fatJar
java -jar build/libs/AISTOCKS_IN_JAVA-1.0.0-SNAPSHOT-all.jar score --asof 2026-03-20
```

### 4. Run tests

```bash
./gradlew test
```

### 5. Heavy research still runs in Python

```bash
# From parent repository root
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python scripts/build_features_duckdb.py ...
pytest -q
```

---

## CLI ↔ Python command map

| Java (`aistocks …`) | Python (`python -m src.cli …`) |
|---------------------|--------------------------------|
| `download-data` | `download-data` |
| `build-universe` | `build-universe` |
| `build-features` | `build-features` |
| `train-baselines` | `train-baselines` |
| `score` | `score` |
| `make-report` | `make-report` |
| `audit-pit` | `audit-pit` (Java prints delegation notice) |
| `audit-survivorship` | `audit-survivorship` |
| `list-universe` | `list-universe` |
| `run` | `run` |

---

## Repo layout (this Java module)

```
AISTOCKS_IN_JAVA/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradle/wrapper/
├── env.example
├── README.md
└── src/main/java/com/aistocks/
    ├── AiStocksApplication.java
    ├── audits/             Audit stubs (delegate to Python for full logic)
    ├── cli/                Picocli commands + .env bootstrap
    ├── config/             Env overlay + credential resolution (Python parity)
    ├── data/fmp/           FMP stable API client (minimal)
    ├── pipelines/          Universe, scoring placeholder, data/report stubs
    └── universe/           AiUniverseRegistry (JSON-backed)
└── src/main/resources/
    └── ai_universe.json    Exported from src/universe/ai_stocks.py
```

Parent repository layout (unchanged) is described in `../README.md` under **Repo Layout**.

---

## Core Design Principles (from parent)

1. **Ranking beats regression.**  
2. **PIT correctness is non-negotiable.**  
3. **Economic validity > statistical fit.**  
4. **Know when to step aside** (G(t)).  
5. **DEV/FINAL holdout is sacred.**

Java porting should preserve these when re-implementing features.

---

## Theoretical Foundation

The parent project implements DEUP-style epistemic uncertainty and conformal intervals for **cross-sectional ranking**. See parent **Theoretical Foundation** and `documentation/CHAPTER_13.md`. Java does not implement this stack yet.

---

## Reproducibility

- Java **placeholder** rankings seed `java.util.Random` with `YYYYMMDD` (same *numeric* seed idea as Python, but **not** the same PRNG — expect different tickers in the top-N vs CPython).  
- Full research reproducibility remains defined by the Python evaluation harness and frozen artifacts under `evaluation_outputs/`.

---

## Documentation

| Document | Description |
|----------|-------------|
| [`../README.md`](../README.md) | Primary project README |
| [`../documentation/RESULTS.md`](../documentation/RESULTS.md) | Chapter results |
| [`../documentation/ROADMAP.md`](../documentation/ROADMAP.md) | Roadmap |
| [`../documentation/CHAPTER_13.md`](../documentation/CHAPTER_13.md) | DEUP methodology |
| This file | Java port status + parity |

---

## Roadmap — Java

1. **Data:** Polygon + Alpha Vantage HTTP clients mirroring Python; SEC user-agent helper using `SEC_CONTACT_EMAIL`.  
2. **Storage:** DuckDB JDBC or embedded native for PIT store parity.  
3. **Features:** Port feature builders or call Python as a subprocess where practical.  
4. **Models:** ONNX export for LGB; or gRPC/Python sidecar for Kronos/FinText.  
5. **Evaluation:** Port walk-forward harness or bind to Parquet/Arrow artifacts produced by Python.  
6. **CI:** GitHub Actions matrix on JDK 21 + 22.

---

## Disclaimer

This repository (Python and Java) is for **research and decision-support** only. It does not provide investment advice, does not execute trades, and makes no guarantees about future performance. The Java port is **incomplete**; do not treat CLI output as a validated trading signal.

---

## Maintainer note: regenerating `ai_universe.json`

If `src/universe/ai_stocks.py` changes in the parent repo:

```bash
python3 <<'PY'
import json, pathlib, runpy
root = pathlib.Path("..")  # adjust to parent src
ns = runpy.run_path(root / "universe" / "ai_stocks.py")
payload = {"categories": ns["AI_UNIVERSE"], "categoryDescriptions": ns["CATEGORY_DESCRIPTIONS"]}
pathlib.Path("src/main/resources/ai_universe.json").write_text(json.dumps(payload, indent=2))
PY
```

(Or keep a small script under `scripts/` — omitted here to avoid scope creep.)
