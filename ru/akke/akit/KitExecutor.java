package ru.akke.akit;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class KitExecutor implements CommandExecutor
{

	private AmazingKit pl;

	public KitExecutor(AmazingKit p)
	{
		this.pl = p;
	}

	public String[] parseArgs(String[] as)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < as.length; i++)
		{
			if (!as[i].equals(""))
			{
				sb = sb.append(as[i].toLowerCase()).append(" ");
			}
		}
		return sb.toString().trim().split(" ");
	}

	public boolean validatePermission(CommandSender s, String p)
	{
		if (!s.hasPermission(p))
		{
			s.sendMessage(pl.getMsg("dontHavePerm"));
			return false;
		}
		else
		{
			return true;
		}
	}

	public boolean validateConsole(CommandSender s)
	{
		if (s.getName().equals("CONSOLE"))
		{
			s.sendMessage(pl.getMsg("consoleCantExecuteThis"));
			return true;
		}
		else
		{
			return false;
		}
	}

	public void processKitList(CommandSender cs)
	{
		StringBuilder sb = new StringBuilder();
		String name;
		for (int i = 0; i < pl.kitList.size(); i++)
		{
			name = pl.kitList.get(i);
			if (cs.hasPermission("amazingkit.kit." + name))
			{
				if (sb.length() != 0)
				{
					sb = sb.append(ChatColor.WHITE).append(", ");
				}
				sb = sb.append(ChatColor.YELLOW).append(name);
				if (cs.hasPermission("amazingkit.manage"))
				{
					sb = sb.append(ChatColor.GREEN).append(" (").append(pl.kitCooldowns.get(name)).append(")");
				}
			}
		}
		if (sb.length() == 0)
		{
			cs.sendMessage(pl.getMsg("noKitsAvailable"));
		}
		else
		{
			cs.sendMessage(pl.getMsg("availableKits") + sb.toString());
		}
	}

	public void processKitCreate(CommandSender cs, String kname)
	{
		if (!validatePermission(cs, "amazingkit.manage"))
		{
			return;
		}
		if (pl.kitList.contains(kname))
		{
			cs.sendMessage(pl.getMsg("kitAlreadyExists"));
			return;
		}

		pl.kitList.add(kname);
		pl.kitCooldowns.put(kname, 0);
		pl.kits.put(kname, new ArrayList<ItemStack>());
		pl.saveConfiguration();

		cs.sendMessage(pl.getMsg("kitCreated").replace("%kname%", kname));
	}

	public void processKitErase(CommandSender cs, String kname)
	{
		if (!validatePermission(cs, "amazingkit.manage"))
		{
			return;
		}
		if (!pl.kitList.contains(kname))
		{
			this.processKitList(cs);
			cs.sendMessage(pl.getMsg("kitNotExists"));
			return;
		}

		pl.kitList.remove(kname);
		pl.kitCooldowns.remove(kname);
		pl.kits.remove(kname);
		pl.saveConfiguration();

		cs.sendMessage(pl.getMsg("kitDeleted").replace("%kname%", kname));
	}

	public void processKitView(CommandSender cs, String kname)
	{
		if (!validatePermission(cs, "amazingkit.manage"))
		{
			return;
		}
		if (!pl.kitList.contains(kname))
		{
			this.processKitList(cs);
			cs.sendMessage(pl.getMsg("kitNotExists"));
			return;
		}
		cs.sendMessage(
				pl.getMsg("kitDescr")
						.replace("%kname%", kname)
						.replace("%cooldown%", pl.kitCooldowns.get(kname).toString())
		);
		for (int i = 0; i < pl.kits.get(kname).size(); i++)
		{
			cs.sendMessage(
					pl.getMsg("kitItemDescr")
							.replace("%index%", Integer.toString(i + 1))
							.replace("%item%", pl.kits.get(kname).get(i).getType().toString())
							.replace("%count%", Integer.toString(pl.kits.get(kname).get(i).getAmount()))
			);
		}
	}

	public void processKitClear(CommandSender cs, String kname)
	{
		if (!validatePermission(cs, "amazingkit.manage"))
		{
			return;
		}
		if (!pl.kitList.contains(kname))
		{
			this.processKitList(cs);
			cs.sendMessage(pl.getMsg("kitNotExists"));
			return;
		}
		pl.kits.get(kname).clear();
		pl.saveConfiguration();
		cs.sendMessage(pl.getMsg("kitCleared").replace("%kname%", kname));
	}

	public void processKitPushItem(CommandSender cs, String kname)
	{
		if (!validatePermission(cs, "amazingkit.manage") || validateConsole(cs))
		{
			return;
		}
		if (!pl.kitList.contains(kname))
		{
			this.processKitList(cs);
			cs.sendMessage(pl.getMsg("kitNotExists"));
			return;
		}
		Player p = (Player) cs;
		if (p.getItemInHand() == null || p.getItemInHand().getTypeId() == 0)
		{
			cs.sendMessage(pl.getMsg("noItemInHand"));
			return;
		}
		ItemStack is = p.getItemInHand().clone();
		pl.kits.get(kname).add(is);
		pl.saveConfiguration();
		cs.sendMessage(
				pl.getMsg("kitItemAdded")
						.replace("%kname%", kname)
						.replace("%item%", is.getType().toString())
		);
	}

	public void processKitSetItem(CommandSender cs, String kname, String itemId)
	{
		if (!validatePermission(cs, "amazingkit.manage") || validateConsole(cs))
		{
			return;
		}
		if (!pl.kitList.contains(kname))
		{
			this.processKitList(cs);
			cs.sendMessage(pl.getMsg("kitNotExists"));
			return;
		}
		Player p = (Player) cs;
		if (p.getItemInHand() == null || p.getItemInHand().getTypeId() == 0)
		{
			cs.sendMessage(pl.getMsg("noItemInHand"));
			return;
		}
		int id = -1;
		try
		{
			id = Integer.parseInt(itemId);
		}
		catch (Exception e)
		{
			id = -1;
		}
		if (id < 1 || id > pl.kits.get(kname).size())
		{
			this.processKitView(cs, kname);
			cs.sendMessage(pl.getMsg("typeCorrectInt"));
			return;
		}
		ItemStack is = p.getItemInHand().clone();
		pl.kits.get(kname).set(id - 1, is);
		pl.saveConfiguration();
		cs.sendMessage(
				pl.getMsg("kitItemSet")
						.replace("%kname%", kname)
						.replace("%item%", is.getType().toString())
						.replace("%id%", Integer.toString(id))
		);
	}

	public void processKitGetItem(CommandSender cs, String kname, String itemId)
	{
		if (!validatePermission(cs, "amazingkit.manage") || validateConsole(cs))
		{
			return;
		}
		if (!pl.kitList.contains(kname))
		{
			this.processKitList(cs);
			cs.sendMessage(pl.getMsg("kitNotExists"));
			return;
		}
		Player p = (Player) cs;
		int id = -1;
		try
		{
			id = Integer.parseInt(itemId);
		}
		catch (Exception e)
		{
			id = -1;
		}
		if (id < 1 || id > pl.kits.get(kname).size())
		{
			this.processKitView(cs, kname);
			cs.sendMessage(pl.getMsg("typeCorrectInt"));
			return;
		}
		ItemStack is = pl.kits.get(kname).get(id - 1).clone();
		p.setItemInHand(is);
		cs.sendMessage(
				pl.getMsg("kitItemGet")
						.replace("%kname%", kname)
						.replace("%item%", is.getType().toString())
						.replace("%id%", Integer.toString(id))
		);
	}

	public void processKitDeleteItem(CommandSender cs, String kname, String itemId)
	{
		if (!validatePermission(cs, "amazingkit.manage"))
		{
			return;
		}
		if (!pl.kitList.contains(kname))
		{
			this.processKitList(cs);
			cs.sendMessage(pl.getMsg("kitNotExists"));
			return;
		}
		int id = -1;
		try
		{
			id = Integer.parseInt(itemId);
		}
		catch (Exception e)
		{
			id = -1;
		}
		if (id < 1 || id > pl.kits.get(kname).size())
		{
			this.processKitView(cs, kname);
			cs.sendMessage(pl.getMsg("typeCorrectInt"));
			return;
		}
		String material = pl.kits.get(kname).get(id - 1).getType().toString();
		pl.kits.get(kname).remove(id - 1);
		pl.saveConfiguration();
		cs.sendMessage(
				pl.getMsg("kitItemDeleted")
						.replace("%kname%", kname)
						.replace("%item%", material)
						.replace("%id%", Integer.toString(id))
		);
	}

	public void processKitHelp(CommandSender cs)
	{
		ArrayList<String> help = new ArrayList<String>();
		help.add("/kit help");
		help.add("/kit [list]");
		help.add("/kit <name>");

		if (cs.hasPermission("amazingkit.manage"))
		{
			help.add("/kit reload");
			help.add("/kit clearcooldown");
			help.add("/kit create <name>");
			help.add("/kit erase <name>");
			help.add("/kit clear <name>");
			help.add("/kit view <name>");
			help.add("/kit add/push/put <name> (with item in hand)");
			help.add("/kit set <name> <position> (with item in hand)");
			help.add("/kit get <name> <position>");
			help.add("/kit delete/remove/del/rem <name> <position>");
			help.add("/kit cooldown <name> <cooldown>");
		}
		cs.sendMessage(pl.getMsg("kitHelp"));
		for (int i = 0; i < help.size(); i++)
		{
			cs.sendMessage("  " + ChatColor.YELLOW + help.get(i));
		}
	}

	public void processKitReload(CommandSender cs)
	{
		if (!validatePermission(cs, "amazingkit.manage"))
		{
			return;
		}
		pl.reloadConfig();
		pl.conf = pl.getConfig();
		pl.loadConfiguration();
		cs.sendMessage(pl.getMsg("confReloaded"));
	}

	public void processKitInfo(CommandSender cs)
	{
		cs.sendMessage(ChatColor.GREEN + "This server is runnung " + pl.getDescription().getFullName());
		cs.sendMessage(ChatColor.GREEN + "http://akkez.ru/AmazingKit/");
	}

	public void processKitClearCooldown(CommandSender cs)
	{
		if (!validatePermission(cs, "amazingkit.manage"))
		{
			return;
		}
		pl.userCooldowns.clear();
		cs.sendMessage(pl.getMsg("cooldownsCleared"));
	}

	public void processKitSetCooldown(CommandSender cs, String kname, String value)
	{
		if (!validatePermission(cs, "amazingkit.manage"))
		{
			return;
		}
		if (!pl.kitList.contains(kname))
		{
			this.processKitList(cs);
			cs.sendMessage(pl.getMsg("kitNotExists"));
			return;
		}
		int val = -1;
		try
		{
			val = Integer.parseInt(value);
		}
		catch (Exception e)
		{
			val = -1;
		}
		if (val < 0)
		{
			cs.sendMessage(pl.getMsg("typeCorrectInt"));
			return;
		}
		pl.kitCooldowns.put(kname, val);
		pl.saveConfiguration();
		cs.sendMessage(
				pl.getMsg("kitSetCooldown")
						.replace("%kname%", kname)
						.replace("%value%", Integer.toString(val))
		);
	}

	public void processKitDispense(CommandSender cs, String kname)
	{
		if (validateConsole(cs))
		{
			return;
		}
		if (!validatePermission(cs, "amazingkit.kit." + kname))
		{
			this.processKitList(cs);
			return;
		}
		if (!pl.kitList.contains(kname))
		{
			this.processKitList(cs);
			cs.sendMessage(pl.getMsg("kitNotExists"));
			return;
		}
		Player p = (Player) cs;
		PlayerInventory inv = p.getInventory();
		int freeSlots = 0;
		ArrayList<Integer> slots = new ArrayList<Integer>();
		for (int i = 0; i < 36; i++)
		{
			if (inv.getItem(i) == null || inv.getItem(i).getTypeId() == 0)
			{
				freeSlots++;
				slots.add(i);
			}
		}
		if (freeSlots < pl.kits.get(kname).size())
		{
			cs.sendMessage(pl.getMsg("notEgoughSlots"));
			return;
		}

		if (pl.userCooldowns.containsKey(kname + ";" + p.getName()))
		{
			long diff = pl.userCooldowns.get(kname + ";" + p.getName()) + ((long)pl.kitCooldowns.get(kname) * 1000) - System.currentTimeMillis();
			if (diff > 0)
			{
				String timeform = null;
				if (pl.locale.equalsIgnoreCase("ru"))
				{
					timeform = pl.getRuTimeform(diff / 1000);
				}
				else
				{
					timeform = pl.getEnTimeform(diff / 1000);
				}

				cs.sendMessage(
						pl.getMsg("kitCooldown")
								.replace("%time%", timeform)
				);
				if (!cs.hasPermission("amazingkit.manage"))
				{
					return;
				}
			}
		}
		pl.userCooldowns.put(kname + ";" + p.getName(), System.currentTimeMillis());
		for (int i = 0; i < pl.kits.get(kname).size(); i++)
		{
			inv.setItem(slots.get(i), pl.kits.get(kname).get(i));
		}
		pl.saveDatabase();
		cs.sendMessage(pl.getMsg("kitDispensed").replace("%kname%", kname));
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String wtf, String[] args0)
	{
		if (!validatePermission(cs, "amazingkit.kit"))
		{
			return true;
		}

		String[] args = parseArgs(args0);

		if ((args[0].equals("") || args[0].equals("list")) && args.length == 1)
		{
			//kit
			//kit list
			processKitList(cs);
			return true;
		}
		if (args[0].equals("reload") && args.length == 1)
		{
			//kit reload
			processKitReload(cs);
			return true;
		}
		if (args[0].equals("help") && args.length == 1)
		{
			//kit help
			processKitHelp(cs);
			return true;
		}
		if (args[0].equals("info") && args.length == 1)
		{
			//kit info
			processKitInfo(cs);
			return true;
		}
		if (args[0].equals("clearcooldown") && args.length == 1)
		{
			//kit clearcooldown
			processKitClearCooldown(cs);
			return true;
		}
		if (args.length == 2 && args[0].equals("create"))
		{
			//kit create <name>
			processKitCreate(cs, args[1]);
			return true;
		}
		if (args.length == 2 && args[0].equals("erase"))
		{
			//kit erase <name>
			processKitErase(cs, args[1]);
			return true;
		}
		if (args.length == 2 && args[0].equals("clear"))
		{
			//kit clear <name>
			processKitClear(cs, args[1]);
			return true;
		}
		if (args.length == 2 && args[0].equals("view"))
		{
			//kit view <name>
			processKitView(cs, args[1]);
			return true;
		}
		if (args.length == 2 && (args[0].equals("put")
				|| args[0].equals("add") || args[0].equals("push")))
		{
			//kit add/put/push <name>, with IS in hand
			processKitPushItem(cs, args[1]);
			return true;
		}
		if (args.length == 3 && args[0].equals("set"))
		{
			//kit set <name> id, with IS in hand
			processKitSetItem(cs, args[1], args[2]);
			return true;
		}
		if (args.length == 3 && args[0].equals("get"))
		{
			//kit get <name> id
			processKitGetItem(cs, args[1], args[2]);
			return true;
		}
		if (args.length == 3 && args[0].equals("cooldown"))
		{
			//kit cooldown <name> value
			processKitSetCooldown(cs, args[1], args[2]);
			return true;
		}
		if (args.length == 3 && (args[0].equals("del") || args[0].equals("rem")
				|| args[0].equals("delete") || args[0].equals("remove")))
		{
			//kit del/rem/delete/remove <name> id
			processKitDeleteItem(cs, args[1], args[2]);
			return true;
		}
		if (args.length == 1)
		{
			//kit <name>
			processKitDispense(cs, args[0]);
			return true;
		}
		cs.sendMessage(pl.getMsg("unknownCommandSyntax"));
		return true;
	}

}