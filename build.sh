#!/bin/bash
set -e

echo "📦 Preparing build environment..."

mkdir -p lib
mkdir -p out

echo "🔧 Building Maven project..."
mvn clean package

# Download OpenTelemetry agent if not already present
AGENT_JAR="lib/opentelemetry-javaagent.jar"
if [ ! -f "$AGENT_JAR" ]; then
  echo "⬇️  Downloading OpenTelemetry Java agent..."
  curl -sSL -o "$AGENT_JAR" https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar
fi

# Set API key (read from environment or fallback to placeholder)
if [[ "$API_KEY" == "<YOUR_HYPERDX_INGESTION_API_KEY>" ]]; then
  echo "⚠️  Please set your HyperDX API key via 'export API_KEY=your-key' or in this script."
  exit 1
fi

echo "🌐 Setting OpenTelemetry environment variables..."
export OTEL_EXPORTER_OTLP_HEADERS="authorization=${API_KEY}"
export JAVA_TOOL_OPTIONS="-javaagent:$(pwd)/$AGENT_JAR"
export OTEL_EXPORTER_OTLP_ENDPOINT="https://in-otel.hyperdx.io"
export OTEL_EXPORTER_OTLP_PROTOCOL="http/protobuf"
export OTEL_LOGS_EXPORTER="otlp"
export OTEL_SERVICE_NAME="tomtest"
export OTEL_LOG_LEVEL="debug"

echo "🚀 Starting instrumented app..."
java \
  -Dlogback.configurationFile=src/main/resources/logback.xml \
  -jar target/hyperdx-java-demo-1.0-SNAPSHOT.jar