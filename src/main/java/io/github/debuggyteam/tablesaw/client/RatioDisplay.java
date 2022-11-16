package io.github.debuggyteam.tablesaw.client;

import net.minecraft.text.Text;
import net.minecraft.util.Nameable;

public enum RatioDisplay implements Nameable {
	NONE("none"),
	RATIO("ratio"),
	OUTPUT_COUNT("output_count")
	;
	
	private final String name;
	
	RatioDisplay(String name) {
		this.name = name;
	}
	
	@Override
	public Text getName() {
		return Text.literal(name);
	}
}
