# Prompt Comparer: LLM Strategy Evaluator

Prompt Comparer is a modern, high-fidelity Android application built using **Kotlin**, **Jetpack Compose (Material 3)**, and **Room Database**. It provides an interactive platform to evaluate and compare how **Zero-Shot**, **Few-Shot**, and **Structured** prompt strategies affect response quality in Large Language Models (LLMs) using Gemini.

---

## 🚀 Key Features

### 1. Real-Time Strategy Pipeline
Execute Zero-Shot, Few-Shot, and Structured prompting strategies **concurrently** on real-world workloads, including:
- **Email Classification & Response Routing**: Customer service email triage.
- **Natural Language to SQL**: Postgres query translation with custom schema constraints.
- **JSON Receipt Extraction**: Structural data extraction from unstructured OCR text.
- **Custom Scenarios**: Write your own prompts and input payloads to run custom experiments.

### 2. LLM-as-a-Judge Evaluation
In addition to user ratings (1 to 5 stars), the app triggers a 4th concurrent LLM call acting as an **AI Prompt Engineering Judge**. The judge meticulously rates all three outputs out of 100 on correctness, constraint adherence, formatting, and brevity, returning a detailed markdown analytical critique.

### 3. Analytics Dashboard
Visualize prompting trends dynamically. The dashboard displays average performance across runs using a **custom-drawn Canvas chart** that shows the comparative superiority of Structured and Few-shot prompting over simple instructions.

### 4. Room Database Persistence
Never lose an experiment. All comparison runs (prompts sent, responses received, user ratings, and AI critiques) are cached locally so you can browse, delete, or load historical runs back into the evaluator.

---

## 🛠️ Prompt Strategies Defined

| Strategy | Description | Example Pattern |
| :--- | :--- | :--- |
| **Zero-Shot** | Simple instruction without examples. High speed, but prone to format violations. | *"Classify this email into Support, Sales, or Billing."* |
| **Few-Shot** | Prompt accompanied by 2-3 examples demonstrating desired behavior. Ideal for style matching. | *"Email: [A] -> Dept: Billing. Email: [B] -> Dept: Sales. Now do this..."* |
| **Structured** | Highly-formatted prompt outlining roles, contexts, and clear XML/JSON/Markdown constraints. | *"# ROLE: cs-assistant \n# CONSTRAINTS: under 3 sentences \n# FORMAT: JSON"* |

---

## 🔑 Setup & API Keys

To use the Prompt Comparer:
1. Open the **Secrets panel** in the Google AI Studio interface.
2. Add your `GEMINI_API_KEY` with your Google AI Studio/Gemini API key.
3. The platform will automatically inject the key into a local `.env` file at build time, securely loading it in code via `BuildConfig.GEMINI_API_KEY`.

---

## 🏗️ Architecture

The app follows **Clean Architecture** combined with **MVVM (Model-View-ViewModel)**:
- `data/api/`: Network models, Retrofit interface, and Gemini API client.
- `data/local/`: Room SQLite database holder and DAO definitions.
- `data/model/`: Central entity schemas.
- `ui/prompt/`: Pre-configured prompt templates, Material 3 layouts, dynamic visual custom-drawn charts, and the `PromptViewModel`.
