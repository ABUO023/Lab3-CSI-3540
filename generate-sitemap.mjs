import { promises as fs } from "node:fs";
import path from "node:path";

const IGNORE_DIRS = new Set(["node_modules", ".git", "target", "dist", "build"]);
const INCLUDE_HTML = new Set([".html"]);
const INCLUDE_CSS = new Set([".css"]);

async function walk(dir, root) {
  const out = [];
  const entries = await fs.readdir(dir, { withFileTypes: true });

  for (const e of entries) {
    if (e.name.startsWith(".")) continue;

    const full = path.join(dir, e.name);
    const rel = path.relative(root, full).replaceAll("\\", "/");

    if (e.isDirectory()) {
      if (IGNORE_DIRS.has(e.name)) continue;
      out.push(...(await walk(full, root)));
    } else {
      const ext = path.extname(e.name).toLowerCase();
      if (!INCLUDE_HTML.has(ext) && !INCLUDE_CSS.has(ext)) continue;
      out.push(rel);
    }
  }

  return out;
}

function makeTitleFromPath(href) {
  return href
    .replace(/\/index\.html$/i, "")
    .replace(/\.html$/i, "")
    .replaceAll("-", " ")
    .replaceAll("_", " ")
    .split("/")
    .filter(Boolean)
    .slice(-2)
    .join(" / ")
    .replace(/\b\w/g, (m) => m.toUpperCase());
}

const root = process.cwd();
const files = await walk(root, root);

const htmlFiles = files.filter((f) => path.extname(f).toLowerCase() === ".html");
const cssFiles = files.filter((f) => path.extname(f).toLowerCase() === ".css");

// Build a map: folder -> css files inside it (including subfolders)
const cssByFolder = new Map();

for (const css of cssFiles) {
  const dir = path.dirname(css); // ex: Book/3.2/styles
  if (!cssByFolder.has(dir)) cssByFolder.set(dir, []);
  cssByFolder.get(dir).push(css);
}

// Helper: find CSS files that are "near" an HTML file
function findCssForHtml(htmlPath) {
  const htmlDir = path.dirname(htmlPath);

  // include css in same folder
  const sameFolder = cssByFolder.get(htmlDir) || [];

  // include css in immediate "styles" folder if exists
  const stylesFolder = cssByFolder.get(`${htmlDir}/styles`) || [];

  // merge + sort
  return [...sameFolder, ...stylesFolder].sort();
}

// Build pages only from index.html files (your use-case)
const pages = htmlFiles
  .filter((p) => p.endsWith("index.html"))
  .map((href) => {
    const title = makeTitleFromPath(href) || href;
    const css = findCssForHtml(href).map((c) => `./${c}`);

    return {
      title,
      href: `./${href}`,
      css,
    };
  })
  .sort((a, b) => a.title.localeCompare(b.title));

await fs.writeFile(
  path.join(root, "site-map.json"),
  JSON.stringify({ pages }, null, 2),
  "utf8"
);

console.log(`Wrote site-map.json with ${pages.length} page(s).`);
