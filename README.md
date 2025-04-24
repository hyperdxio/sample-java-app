# HyperDX Java Tracing Demo

This is a minimal Java application that demonstrates how to send logs and distributed traces to [HyperDX](https://www.hyperdx.io) using the [OpenTelemetry Java Agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation).

The app:
- Starts an embedded HTTP server on `localhost:8080`
- Exposes several endpoints that simulate workflows, internal processing, and external calls
- Uses the OpenTelemetry Java agent to auto-instrument traces and logs
- Sends all observability data to HyperDX with minimal setup

---

## ✨ Features

- ✅ Auto-instrumented HTTP server with SLF4J (Logback)
- ✅ Rich logs with `trace_id` and `span_id` included
- ✅ Simulated internal and external requests (e.g., `httpbin.org`)
- ✅ A `/full-run` route that triggers all endpoints in sequence to showcase full trace trees
- ✅ One-command setup via `./build.sh`

---

## 🛠 Requirements

- Java 21+
- Maven 3.8+
- [HyperDX account](https://www.hyperdx.io) and API key
- Internet access to download the OpenTelemetry Java agent

---

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/hyperdx-java-demo.git
cd hyperdx-java-demo
```

### 2. Set Your API Key

```bash
export API_KEY="your-hyperdx-api-key"
```

### 3. Run the Project (build, download agent, launch)

```bash
./build.sh
```

> ☝️ This script:
> - Compiles the app with Maven
> - Downloads the OpenTelemetry Java agent
> - Sets the required environment variables
> - Runs the app with instrumentation enabled

---

## 🔍 Endpoints

| Route              | Description                            |
|-------------------|----------------------------------------|
| `/hello`          | Basic response with traceable log      |
| `/user?id=123`    | Simulates a DB-like operation          |
| `/order?id=999`   | Simulates an external HTTP call        |
| `/workflow`       | Calls `/user` and `/order` internally  |
| `/full-run`       | Calls all the above to simulate a full trace |

---

## 📈 What You'll See in HyperDX

- Each route generates a trace with logs and spans
- `/full-run` builds a full nested trace tree like this:

```
/full-run
├── /hello
├── /user?id=123
├── /order?id=999
│   └── https://httpbin.org/delay/1
└── /workflow
    ├── /user?id=123
    └── /order?id=999
        └── https://httpbin.org/delay/1
```

- Logs attached to each span contain `trace_id` and `span_id`

---

## 🧩 Customization Ideas

- Add error handling or latency simulation
- Add custom span attributes (e.g. `user.id`)
- Build a second service to simulate cross-service traces
- Add metrics using OTEL SDK

---

## 📄 License

MIT – feel free to use, fork, modify, and contribute.