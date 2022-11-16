package io.github.debuggyteam.tablesaw.client;

import org.quiltmc.config.api.Config;
import org.quiltmc.config.api.WrappedConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.config.api.annotations.Processor;

@Processor("setSerializer")
public class ClientSettings extends WrappedConfig {
	@Comment("How to display ratios on recipe icons in the TableSaw interface.")
	public final RatioDisplay iconRatios = RatioDisplay.OUTPUT_COUNT;
	
	@Comment("If true, overrides the name in recipe tooltips to show the input -> output ratio.")
	public final Boolean ratioTooltip = true;
	
	public ClientSettings() {}
	
	public void setSerializer(Config.Builder builder) {
		builder.format("json5");
	}
}
