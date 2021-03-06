package chylex.hee.world.structure;
import java.util.Random;
import net.minecraft.block.Block;
import chylex.hee.system.abstractions.BlockInfo;

@FunctionalInterface
public interface IBlockPicker{
	public static IBlockPicker basic(final Block block){
		return new BlockInfo(block);
	}
	
	public static IBlockPicker basic(final Block block, final int meta){
		return new BlockInfo(block,meta);
	}
	
	BlockInfo pick(Random rand);
}
