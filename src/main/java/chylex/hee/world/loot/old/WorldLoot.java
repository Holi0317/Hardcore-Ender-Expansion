package chylex.hee.world.loot.old;
import static net.minecraftforge.common.ChestGenHooks.*;
import java.util.Random;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import chylex.hee.init.BlockList;
import chylex.hee.init.ItemList;
import chylex.hee.item.ItemKnowledgeNote;
import chylex.hee.system.logging.Stopwatch;
import cpw.mods.fml.common.registry.VillagerRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry.IVillageTradeHandler;

public class WorldLoot implements IVillageTradeHandler{
	public static void registerWorldLoot(){
		Stopwatch.time("WorldLoot - register");
		
		// KNOWLEDGE FRAGMENTS
		WeightedRandomChestContent item = new WeightedRandomKnowledgeNote(6);
		for(String s:new String[]{
			DUNGEON_CHEST, PYRAMID_DESERT_CHEST, PYRAMID_JUNGLE_CHEST, VILLAGE_BLACKSMITH
		})getInfo(s).addItem(item);
		
		item = new WeightedRandomKnowledgeNote(7);
		getInfo(MINESHAFT_CORRIDOR).addItem(item);
		
		item = new WeightedRandomKnowledgeNote(9);
		for(String s:new String[]{
			STRONGHOLD_CORRIDOR, STRONGHOLD_CROSSING, STRONGHOLD_LIBRARY
		})getInfo(s).addItem(item);
		
		// MISC
		VillagerRegistry.instance().registerVillageTradeHandler(1,new WorldLoot());

		Stopwatch.finish("WorldLoot - register");
	}
	
	private WorldLoot(){}
	
	@Override
	public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random rand){
		if (villager.getProfession() == 1){
			if (rand.nextFloat() < 0.65F)recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald,4+rand.nextInt(4)),ItemKnowledgeNote.setRandomNote(new ItemStack(ItemList.knowledge_note),rand,2)));
			else if (rand.nextFloat() < 0.3F)recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald,11+rand.nextInt(5),0),new ItemStack(BlockList.essence_altar)));
		}
	}
}
