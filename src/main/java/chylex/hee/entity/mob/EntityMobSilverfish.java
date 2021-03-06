package chylex.hee.entity.mob;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import chylex.hee.entity.mob.ai.EntityAIHideInBlock;
import chylex.hee.entity.mob.ai.EntityAIRandomTarget;
import chylex.hee.entity.mob.ai.EntityAISummonFromBlock;
import chylex.hee.entity.mob.ai.EntityAIWanderConstantly;
import chylex.hee.init.ItemList;
import chylex.hee.system.abstractions.BlockInfo;
import chylex.hee.system.abstractions.damage.Damage;
import chylex.hee.system.abstractions.damage.DamageUtil;
import chylex.hee.system.abstractions.damage.IDamageModifier;
import chylex.hee.system.util.DragonUtil;
import chylex.hee.world.loot.PercentageLootTable;
import chylex.hee.world.loot.info.LootMobInfo;

public class EntityMobSilverfish extends EntitySilverfish{
	private static final PercentageLootTable drops = new PercentageLootTable();
	
	static{
		drops.addLoot(ItemList.ancient_dust).setChances(obj -> {
			final boolean critical = DamageUtil.isCriticalHit(((LootMobInfo)obj).entity.func_94060_bK());
			return critical ? new float[]{ 0.75F, 0.15F } : new float[]{ 0.15F };
		});
	}
	
	private EntityAISummonFromBlock canSummonSilverfish;
	private EntityAIHideInBlock canHideInBlocks;
	
	public EntityMobSilverfish(World world){
		super(world);
		setSize(0.35F,0.6F);
		
		tasks.addTask(1,new EntityAISwimming(this));
		tasks.addTask(3,new EntityAIAttackOnCollide(this,EntityPlayer.class,1D,false));
		tasks.addTask(4,new EntityAIWanderConstantly(this,1D));
		setCanSummonSilverfish(true);
		setCanHideInBlocks(true);
		
		targetTasks.addTask(1,new EntityAIHurtByTarget(this,false));
		targetTasks.addTask(2,new EntityAIRandomTarget(this,EntityPlayer.class,true,true));
		
		experienceValue = 3;
	}
	
	public void setCanSummonSilverfish(boolean allow){
		if (allow && canSummonSilverfish == null){
			canSummonSilverfish = new EntityAISummonFromBlock(this,Blocks.monster_egg,world -> new EntityMobSilverfish(world));
			tasks.addTask(2,canSummonSilverfish);
		}
		else if (!allow && canSummonSilverfish != null){
			tasks.removeTask(canSummonSilverfish);
			canSummonSilverfish = null;
		}
	}
	
	public void setCanHideInBlocks(boolean allow){
		if (allow && canHideInBlocks == null){
			canHideInBlocks = new EntityAIHideInBlock(this,new Block[]{ Blocks.cobblestone, Blocks.stone, Blocks.stonebrick },target -> new BlockInfo(Blocks.monster_egg,BlockSilverfish.func_150195_a(target.block,target.meta)));
			tasks.addTask(5,canHideInBlocks);
		}
		else if (!allow && canHideInBlocks != null){
			tasks.removeTask(canHideInBlocks);
			canHideInBlocks = null;
		}
	}
	
	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(8D);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
		getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(2D);
		getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(8D);
	}
	
	@Override
	public void onUpdate(){
		if (worldObj.isRemote)rotationYaw = DragonUtil.rotateSmoothly(rotationYaw,rotationYawHead,30F);
		super.onUpdate();
	}
	
	@Override
	protected void dropFewItems(boolean recentlyHit, int looting){
		for(ItemStack drop:drops.generateLoot(new LootMobInfo(this,recentlyHit,looting),rand))entityDropItem(drop,0F);
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount){
		if (isEntityInvulnerable())return false;
		
		if (canSummonSilverfish != null && (source.getEntity() != null || source == DamageSource.magic))canSummonSilverfish.setSummonTimer(20);
		return super.attackEntityFrom(source,amount);
	}
	
	@Override
	public boolean attackEntityAsMob(Entity target){
		if (Damage.hostileMob(this).addModifiers(IDamageModifier.rapidDamage(5),IDamageModifier.overrideKnockback(0.25F+rand.nextFloat()*0.25F)).deal(target)){
			if (rand.nextInt(4) == 0 && worldObj.getEntitiesWithinAABB(EntitySilverfish.class,boundingBox.expand(12D,6D,12D)).stream().anyMatch(mob -> mob != this && ((EntitySilverfish)mob).getAttackTarget() == target)){
				List<EntityLivingBase> targets = worldObj.getEntitiesWithinAABB(EntityPlayer.class,boundingBox.expand(4D,4D,4D));
				targets = targets.stream().filter(entity -> entity.getDistanceSqToEntity(this) <= 64D && getEntitySenses().canSee(entity)).collect(Collectors.toList());
				
				if (!targets.isEmpty())setAttackTarget(targets.get(rand.nextInt(targets.size())));
			}
			
			return true;
		}
		else return false;
	}
	
	@Override
	protected boolean isAIEnabled(){
		return true;
	}
	
	@Override
	public String getCommandSenderName(){
		return hasCustomNameTag() ? getCustomNameTag() : StatCollector.translateToLocal("entity.Silverfish.name");
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound nbt){
		super.writeEntityToNBT(nbt);
		nbt.setBoolean("canSummonSilverfish",canSummonSilverfish != null);
		nbt.setBoolean("canHideInBlocks",canHideInBlocks != null);
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound nbt){
		super.readEntityFromNBT(nbt);
		setCanSummonSilverfish(nbt.getBoolean("canSummonSilverfish"));
		setCanHideInBlocks(nbt.getBoolean("canHideInBlocks"));
	}
}
