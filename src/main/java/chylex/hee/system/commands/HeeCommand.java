package chylex.hee.system.commands;
import chylex.hee.mechanics.misc.Baconizer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

abstract class HeeCommand extends CommandBase{
	private final String cmdName;
	
	HeeCommand(String cmdName){
		this.cmdName = cmdName;
	}
	
	@Override
	public String getCommandName(){
		return cmdName;
	}

	@Override
	public String getCommandUsage(ICommandSender sender){
		return "/"+cmdName;
	}
	
	@Override
	public int getRequiredPermissionLevel(){
		return 4;
	}
	
	protected final void sendMessage(ICommandSender receiver, String message){
		receiver.addChatMessage(new ChatComponentText(Baconizer.sentence(message)));
	}
}
