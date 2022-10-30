package io.github.debuggyteam.tablesaw.api;

/**
 * Use this class as a "tablesaw" entrypoint in your quilt.mod.json or fabric.mod.json, and you can dynamically register
 * recipes any time the recipe table is rebuilt.
 */
public interface TableSawCompat {
	public void run(TableSawAPI api);
}
