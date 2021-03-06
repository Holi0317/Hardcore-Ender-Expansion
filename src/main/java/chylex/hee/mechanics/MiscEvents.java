package chylex.hee.mechanics;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import chylex.hee.init.ItemList;
import chylex.hee.item.ItemTransferenceGem;
import chylex.hee.mechanics.enhancements.EnhancementHandler;
import chylex.hee.mechanics.enhancements.types.TransferenceGemEnhancements;
import chylex.hee.system.util.ItemUtil;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class MiscEvents{
	/*
	 * Silverfish dropping blood
	 */
	@SubscribeEvent
	public void onLivingDrops(LivingDropsEvent e){
		if (e.entity.worldObj.isRemote || !e.recentlyHit)return;
		
		if (e.entity.getClass() == EntitySilverfish.class && e.entityLiving.getRNG().nextInt(14-Math.min(e.lootingLevel,4)) == 0){
			boolean drop = e.entityLiving.getRNG().nextInt(4) == 0;
			boolean isPlayer = e.source.getEntity() instanceof EntityPlayer;
			
			if (!drop && isPlayer){
				ItemStack held = ((EntityPlayer)e.source.getEntity()).inventory.getCurrentItem();
				if (held != null && held.getItem() == Items.golden_sword)drop = true;
			}
			
			if (drop){
				EntityItem item = new EntityItem(e.entity.worldObj,e.entity.posX,e.entity.posY,e.entity.posZ,new ItemStack(ItemList.silverfish_blood));
				item.delayBeforeCanPickup = 10;
				e.drops.add(item);
			}
		}
	}
	
	/*
	 * Right-clicking on item frame, mob and item with Transference Gem
	 */
	@SubscribeEvent
	public void onPlayerInteractEntity(EntityInteractEvent e){
		if (e.entity.worldObj.isRemote)return;
		
		if (e.target instanceof EntityItemFrame){
			EntityItemFrame itemFrame = (EntityItemFrame)e.target;
			
			ItemStack is = itemFrame.getDisplayedItem();
			
			if (is == null || is.getItem() != ItemList.transference_gem || e.entityPlayer.isSneaking())return;
			else if (EnhancementHandler.hasEnhancement(is,TransferenceGemEnhancements.TOUCH)){
				is = ((ItemTransferenceGem)ItemList.transference_gem).tryTeleportEntity(is,e.entityPlayer,e.entityPlayer);
				ItemUtil.getTagRoot(is,false).removeTag("cooldown");
				
				itemFrame.setDisplayedItem(is);
				e.setCanceled(true);
			}
		}
	}
}
