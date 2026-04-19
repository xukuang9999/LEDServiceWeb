import { cpSync, mkdirSync, readFileSync, rmSync, writeFileSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const scriptDir = dirname(fileURLToPath(import.meta.url));
const frontendDir = resolve(scriptDir, "..");
const projectRoot = resolve(frontendDir, "..");
const distDir = resolve(frontendDir, "dist");
const springStaticCmsRoot = resolve(projectRoot, "zhglxt-web/src/main/resources/static/cms");
const cloudrunCmsRoot = resolve(projectRoot, "cloudrun-static/site/zhglxt/cms");

const modernTargets = [
  resolve(projectRoot, "zhglxt-web/src/main/resources/static/modern"),
  resolve(projectRoot, "cloudrun-static/site/zhglxt/modern"),
];

const modernRoutes = [
  "",
  "index",
  "about",
  "contact",
  "projects",
  "rental-service",
  "led-products",
  "solution",
  "solutions",
  "exhibition",
  "exhibition-detail",
  "hospitality",
  "hospitality-exhibition",
  "products",
  "exhibition-products",
  "news",
  "news-detail",
  "service",
  "service-detail",
  "develop-history",
];

const cmsMetadataFiles = ["robots.txt", "llms.txt", "llms-full.txt", "sitemap.xml"];

// Keep legacy Cloud Run static entry files as redirect shims without touching
// the Thymeleaf templates that the Spring MVC controllers still render.
const legacyRedirects = [
  {
    targetPath: "/zhglxt/modern/",
    files: ["cloudrun-static/site/zhglxt/cms/index.html"],
  },
  {
    targetPath: "/zhglxt/modern/about/",
    files: ["cloudrun-static/site/zhglxt/cms/about.html"],
  },
  {
    targetPath: "/zhglxt/modern/contact/",
    files: ["cloudrun-static/site/zhglxt/cms/contact.html"],
  },
  {
    targetPath: "/zhglxt/modern/projects/",
    files: ["cloudrun-static/site/zhglxt/cms/projects.html"],
  },
  {
    targetPath: "/zhglxt/modern/rental-service/",
    files: ["cloudrun-static/site/zhglxt/cms/rental-service.html"],
  },
  {
    targetPath: "/zhglxt/modern/led-products/",
    files: ["cloudrun-static/site/zhglxt/cms/led-products.html"],
  },
  {
    targetPath: "/zhglxt/modern/service/",
    files: [],
  },
  {
    targetPath: "/zhglxt/modern/news/",
    files: [],
  },
  {
    targetPath: "/zhglxt/modern/solutions/",
    files: [],
  },
  {
    targetPath: "/zhglxt/modern/hospitality-exhibition/",
    files: [],
  },
  {
    targetPath: "/zhglxt/modern/exhibition-products/",
    files: [],
  },
  {
    targetPath: "/zhglxt/modern/develop-history/",
    files: [],
  },
  {
    targetPath: "/zhglxt/modern/",
    files: [
      "cloudrun-static/site/zhglxt/cms/404.html",
      "zhglxt-web/src/main/resources/static/cms/404.html",
    ],
  },
];

function ensureCleanCopy(from, to) {
  rmSync(to, { force: true, recursive: true });
  mkdirSync(dirname(to), { recursive: true });
  cpSync(from, to, { recursive: true });
}

function writeRouteEntry(modernRoot, route, html) {
  const routeDir = join(modernRoot, route);
  mkdirSync(routeDir, { recursive: true });
  writeFileSync(join(routeDir, "index.html"), html);
}

function buildRedirectHtml(targetPath) {
  const escapedPath = JSON.stringify(targetPath);
  return `<!doctype html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <meta name="robots" content="noindex,follow" />
    <title>Redirecting...</title>
    <link rel="canonical" href="${targetPath}" />
    <meta http-equiv="refresh" content="0; url=${targetPath}" />
    <script>
      const target = ${escapedPath};
      const next = target + window.location.search + window.location.hash;
      window.location.replace(next);
    </script>
  </head>
  <body>
    <main style="font-family: 'Space Grotesk', Arial, sans-serif; padding: 40px; line-height: 1.6;">
      <h1 style="margin: 0 0 12px;">Redirecting to the current public page</h1>
      <p style="margin: 0 0 12px;">
        This public page has moved to the current LED Service route.
      </p>
      <p style="margin: 0;">
        If the redirect does not happen automatically, <a href="${targetPath}">open the updated page</a>.
      </p>
    </main>
  </body>
</html>
`;
}

const routeShell = readFileSync(join(distDir, "index.html"), "utf8");

for (const target of modernTargets) {
  ensureCleanCopy(distDir, target);
  for (const route of modernRoutes) {
    writeRouteEntry(target, route, routeShell);
  }
}

for (const redirect of legacyRedirects) {
  const html = buildRedirectHtml(redirect.targetPath);
  for (const relativeFile of redirect.files) {
    const absoluteFile = resolve(projectRoot, relativeFile);
    mkdirSync(dirname(absoluteFile), { recursive: true });
    writeFileSync(absoluteFile, html);
  }
}

mkdirSync(cloudrunCmsRoot, { recursive: true });
for (const fileName of cmsMetadataFiles) {
  cpSync(join(springStaticCmsRoot, fileName), join(cloudrunCmsRoot, fileName));
}
