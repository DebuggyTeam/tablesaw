package io.github.debuggyteam.tablesaw;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.StonecutterBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class TableSawBlock extends StonecutterBlock {
	// Think of this as "minecraft:container/tablesaw:tablesaw"
	// the concept of a "container" is owned by vanilla, but we own this particular container and need to namespace it
	// for protection in case we need to put a second container block in.
    private static final Text GUI_TITLE = Text.translatable("container.tablesaw.tablesaw");
    
    private static final double PX = 1/16d;
    private static final double TOP_THICKNESS = 3*PX;
    private static final double FULL_HEIGHT = 16*PX;
    private static final double FULL_WIDTH = 16*PX;
    private static final double LEG_WIDTH = 3*PX;
    private static final double LEG_HEIGHT = FULL_HEIGHT - TOP_THICKNESS;
    
    protected static final VoxelShape TOP_PLATE = VoxelShapes.cuboid(0, FULL_HEIGHT - TOP_THICKNESS, 0, FULL_WIDTH, FULL_HEIGHT, FULL_WIDTH);
    protected static final VoxelShape NW_LEG = VoxelShapes.cuboid(0,0,0,LEG_WIDTH,LEG_HEIGHT,LEG_WIDTH);
    protected static final VoxelShape NE_LEG = VoxelShapes.cuboid(FULL_WIDTH-LEG_WIDTH, 0, 0, FULL_WIDTH, LEG_HEIGHT, LEG_WIDTH);
    protected static final VoxelShape SW_LEG = VoxelShapes.cuboid(0,0,FULL_WIDTH-LEG_WIDTH, LEG_WIDTH, LEG_HEIGHT, FULL_WIDTH);
    protected static final VoxelShape SE_LEG = VoxelShapes.cuboid(FULL_WIDTH-LEG_WIDTH, 0, FULL_WIDTH-LEG_WIDTH, FULL_WIDTH, LEG_HEIGHT, FULL_WIDTH);
    
    protected static final VoxelShape SHAPE = VoxelShapes.union(TOP_PLATE, NW_LEG, NE_LEG, SW_LEG, SE_LEG);
    
    public TableSawBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        } else {
            player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
            return ActionResult.CONSUME;
        }
    }

    @Nullable
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, player) -> new TableSawScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, pos)), GUI_TITLE
        );
    }
}
