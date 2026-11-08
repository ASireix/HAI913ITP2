import { instance } from '@viz-js/viz';
import { readFile, writeFile } from 'node:fs/promises';
import { resolve } from 'node:path';

const viz = await instance();
console.log(`Graphviz ${viz.graphvizVersion}`);
for (const directory of process.argv.slice(2)) {
  const base = resolve(directory);
  const dot = await readFile(resolve(base, 'dendrogram.dot'), 'utf8');
  const svg = viz.renderString(dot, { format: 'svg', engine: 'dot' });
  await writeFile(resolve(base, 'dendrogram.svg'), svg);
  console.log(resolve(base, 'dendrogram.svg'));
}
