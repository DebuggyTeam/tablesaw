package io.github.debuggyteam.tablesaw.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.debuggyteam.tablesaw.TableSaw;
import io.github.debuggyteam.tablesaw.api.TableSawRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TableSawEmiRecipe implements EmiRecipe {
    Identifier id;
    EmiIngredient input;
    EmiStack output;

    // somehow get the IDs for the tablesaw recipes
    public TableSawEmiRecipe(TableSawRecipe recipe) {
        ItemStack itemStack = new ItemStack(recipe.getInput(), recipe.getResult().getCount());
        input = EmiIngredient.of(Ingredient.ofStacks(itemStack));
        output = EmiStack.of(recipe.getResult());
        id = new Identifier(
                TableSaw.MODID,
                "/" +
                        mangle(Registry.ITEM.getId(itemStack.getItem())) +
                        "_" +
                        itemStack.getCount() +
                        "__" +
                        mangle(output.getId()) +
                        "_" +
                        output.getAmount()
        );
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TableSawEmiIntegration.TABLESAW_CATEGORY;
    }

    @Override
    public @Nullable Identifier getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(input);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(output);
    }

    @Override
    public int getDisplayWidth() {
        return 76;
    }

    @Override
    public int getDisplayHeight() {
        return 18;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 21, 1);
        widgets.addSlot(input, 0, 0);
        widgets.addSlot(output, 58, 0).recipeContext(this);
    }
    
    private String mangle(Identifier id) {
        return id.getNamespace() + "_" + id.getPath();
    }
}
