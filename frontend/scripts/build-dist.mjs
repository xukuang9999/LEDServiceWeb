import { rmSync, symlinkSync } from "node:fs";
import { join, resolve } from "node:path";
import { tmpdir } from "node:os";
import { spawnSync } from "node:child_process";
import { fileURLToPath } from "node:url";

const scriptDir = resolve(fileURLToPath(new URL(".", import.meta.url)));
const frontendDir = resolve(scriptDir, "..");
const projectRoot = resolve(frontendDir, "..");
const tempProjectLink = join(tmpdir(), "led-service-build-root");

let result;

rmSync(tempProjectLink, { force: true, recursive: true });

try {
  symlinkSync(projectRoot, tempProjectLink);

  result = spawnSync("npx", ["vite", "build"], {
    cwd: join(tempProjectLink, "frontend"),
    stdio: "inherit",
  });
} finally {
  rmSync(tempProjectLink, { force: true, recursive: true });
}

if (typeof result?.status === "number") {
  process.exit(result.status);
}

process.exit(1);
