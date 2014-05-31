package ru.akke.akit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import org.mcstats.Metrics;

public class AmazingKit extends JavaPlugin
{
	public FileConfiguration conf;
	public Logger log;
	public HashMap<String, Long> userCooldowns = new HashMap<String, Long>(); //nick.kit -> timestamp
	public HashMap<String, ArrayList<ItemStack>> kits = new HashMap<String, ArrayList<ItemStack>>();
	public HashMap<String, Integer> kitCooldowns = new HashMap<String, Integer>();
	public ArrayList<String> kitList = new ArrayList<String>();
	public String firstJoinKit;
	public String locale;

	@Override
	public void onEnable()
	{
		log = getServer().getLogger();

		if (!this.getDataFolder().exists())
		{
			this.getDataFolder().mkdir();
			_log("Created " + this.getDataFolder() + " dir");
		}

		conf = getConfig();
		loadConfiguration();
		loadDatabase();

		getCommand("kit").setExecutor(new KitExecutor(this));
		getServer().getPluginManager().registerEvents(new KitListener(this), this);

		try
		{
			Metrics metrics = new Metrics(this);
			metrics.start();
		}
		catch (Exception e)
		{
			_log("Failed to enable PluginMetrics!");
		}

		_log("Enabled!");
	}

	@Override
	public void onDisable()
	{
		saveDatabase();
		_log("Disabled!");
	}

	public void _log(String arg)
	{
		log.info("[" + this.getDescription().getName() + "] " + arg);
	}

	@SuppressWarnings("unchecked")
	public void loadDatabase()
	{
		File f = new File(getDataFolder() + "/cooldowns.dat");
		if (f.exists())
		{
			try
			{
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
				Object m = ois.readObject();
				this.userCooldowns = (HashMap<String, Long>) m;
				ois.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void saveDatabase()
	{
		String kitname;
		String s;
		Iterator<String> it = this.userCooldowns.keySet().iterator();
		while (it.hasNext())
		{
			s = it.next();
			kitname = s.split(";")[0];
			if (!this.kitCooldowns.containsKey(kitname))
			{
				it.remove();
				continue;
			}
			if (this.userCooldowns.get(s) + (long)this.kitCooldowns.get(kitname) * 1000 - System.currentTimeMillis() < 0)
			{
				it.remove();
			}
		}
		File f = new File(getDataFolder() + "/cooldowns.dat");
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
			oos.writeObject(this.userCooldowns);
			oos.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void loadConfiguration()
	{
		conf.addDefault("messages.ru.dontHavePerm", "&cУ вас нет прав!");
		conf.addDefault("messages.ru.consoleCantExecuteThis", "&cНельзя запустить это из консоли!");
		conf.addDefault("messages.ru.noKitsAvailable", "&eНет доступных наборов");
		conf.addDefault("messages.ru.availableKits", "&bСписок доступных наборов: ");
		conf.addDefault("messages.ru.kitAlreadyExists", "&cЭтот набор уже существует!");
		conf.addDefault("messages.ru.kitCreated", "&bНабор '&e%kname%&b' создан!");
		conf.addDefault("messages.ru.kitNotExists", "&cЭтого набора не существует!");
		conf.addDefault("messages.ru.kitDeleted", "&bНабор '&e%kname%&b' удален!");
		conf.addDefault("messages.ru.kitDescr", "&bНабор '&e%kname%&b' - перезарядка &e%cooldown%&b секунд");
		conf.addDefault("messages.ru.kitItemDescr", "&b  Предмет &e#%index%&b: &a%item%&b x &a%count%");
		conf.addDefault("messages.ru.kitCleared", "&bНабор '&e%kname%&b' очищен!");
		conf.addDefault("messages.ru.noItemInHand", "&cУ вас нет предмета в руке!");
		conf.addDefault("messages.ru.typeCorrectInt", "&cВведите корректное число!");
		conf.addDefault("messages.ru.kitSetCooldown",
				"&bВ наборе '&e%kname%&b' установлена перезарядка в &e%value%&b секунд");
		conf.addDefault("messages.ru.notEgoughSlots", "&cНедостаточно свободного места в инвентаре!");
		conf.addDefault("messages.ru.kitCooldown", "&cЭтот набор можно получить только через %time%");
		conf.addDefault("messages.ru.kitDispensed", "&bВыдан набор '&e%kname%&b'");
		conf.addDefault("messages.ru.kitItemAdded", "&bВ набор '&e%kname%&b' добавлен предмет &e%item%");
		conf.addDefault("messages.ru.kitItemSet",
				"&bВ набор '&e%kname%&b' на место &e%id%&b добавлен предмет &e%item%");
		conf.addDefault("messages.ru.kitItemGet",
				"&bИз набора '&e%kname%&b' выдан предмет &e%item%&b под номером &e%id%");
		conf.addDefault("messages.ru.kitItemDeleted",
				"&bИз набора '&e%kname%&b' удален предмет &e%item%&b под номером &e%id%");
		conf.addDefault("messages.ru.kitHelp", "&bСправка: ");
		conf.addDefault("messages.ru.confReloaded", "&bКонфигурационный файл перезагружен.");
		conf.addDefault("messages.ru.cooldownsCleared", "&bКулдаун китов обнулен.");
		conf.addDefault("messages.ru.unknownCommandSyntax", "&cНеверный синтаксис команды!");

		conf.addDefault("messages.en.dontHavePerm", "&cYou don't have permission!");
		conf.addDefault("messages.en.consoleCantExecuteThis", "&cCan't execute this from console!");
		conf.addDefault("messages.en.noKitsAvailable", "&eList of available kits is empty.");
		conf.addDefault("messages.en.availableKits", "&bList of available kits: ");
		conf.addDefault("messages.en.kitAlreadyExists", "&cThis kit already exists!");
		conf.addDefault("messages.en.kitCreated", "&bKit '&e%kname%&b' created!");
		conf.addDefault("messages.en.kitNotExists", "&cThis kit isn't exists!");
		conf.addDefault("messages.en.kitDeleted", "&bKit '&e%kname%&b' deleted!");
		conf.addDefault("messages.en.kitDescr", "&bKit '&e%kname%&b' - &e%cooldown%&b seconds cooldown");
		conf.addDefault("messages.en.kitItemDescr", "&b  Item &e#%index%&b: &a%item%&b x &a%count%");
		conf.addDefault("messages.en.kitCleared", "&bKit '&e%kname%&b' cleared!");
		conf.addDefault("messages.en.noItemInHand", "&cYou don't have any item in hand!");
		conf.addDefault("messages.en.typeCorrectInt", "&cType corrent integer!");
		conf.addDefault("messages.en.kitSetCooldown", "&bIn the kit '&e%kname%&b' is set cooldown &e%value%&b seconds");
		conf.addDefault("messages.en.notEgoughSlots", "&cNot enough free space in inventory!");
		conf.addDefault("messages.en.kitCooldown", "&cYou can get this kit after %time%");
		conf.addDefault("messages.en.kitDispensed", "&bIssued kit '&e%kname%&b'");
		conf.addDefault("messages.en.kitItemAdded", "&bAdded item &e%item%&b in kit '&e%kname%&b'");
		conf.addDefault("messages.en.kitItemSet", "&bAdded item &e%item%&b in kit '&e%kname%&b' on position &e%id%");
		conf.addDefault("messages.en.kitItemGet", "&bIssued item &e%item%&b from kit '&e%kname%&b' on position &e%id%");
		conf.addDefault("messages.en.kitItemDeleted",
				"&bRemoved item &e%item%&b in kit '&e%kname%&b' on position &e%id%");
		conf.addDefault("messages.en.kitHelp", "&bHelp: ");
		conf.addDefault("messages.en.confReloaded", "&bConfiguration file reloaded.");
		conf.addDefault("messages.en.cooldownsCleared", "&bUser cooldowns dropped.");
		conf.addDefault("messages.en.unknownCommandSyntax", "&cWrong command syntax!");

		conf.addDefault("kitOnFirstJoin", "none");
		conf.addDefault("locale", "en");

		conf.options().copyDefaults(true);
		this.saveConfig();
		this.reloadConfig();
		conf = this.getConfig();

		this.firstJoinKit = conf.getString("kitOnFirstJoin", "none");
		this.locale = conf.getString("locale", "en");

		kits.clear();
		kitCooldowns.clear();
		kitList.clear();

		MemorySection ms = (MemorySection) conf.get("kits");
		if (ms != null)
		{
			for (String kitname : ms.getKeys(false))
			{
				kitname = kitname.toLowerCase();
				kitList.add(kitname);
				kitCooldowns.put(kitname, conf.getInt("kits." + kitname + ".cooldown", 0));
				kits.put(kitname, new ArrayList<ItemStack>());
				int i = 1;
				while (conf.isItemStack("kits." + kitname + ".item" + i))
				{
					kits.get(kitname).add(conf.getItemStack("kits." + kitname + ".item" + i));
					i++;
				}
			}
		}
	}

	public void saveConfiguration()
	{
		conf.set("kitOnFirstJoin", this.firstJoinKit);
		conf.set("kits", null);
		String name;
		for (int i = 0; i < kitList.size(); i++)
		{
			name = kitList.get(i);
			conf.set("kits." + name + ".cooldown", kitCooldowns.get(name));

			for (int j = 0; j < kits.get(name).size(); j++)
			{
				conf.set("kits." + name + ".item" + (j + 1), kits.get(name).get(j));
			}
		}

		this.saveConfig();
		this.reloadConfig();
		conf = this.getConfig();
	}

	public String getMsg(String arg)
	{
		return conf.getString("messages." + this.locale + "." + arg, "&4Configuration key [" + arg + "] not found")
				.replace("&", "§");
	}

	public String getRuTimeform(long arg)
	{
		long days = 0;
		long hours = 0;
		long minutes = 0;
		String res = "";
		String wordForm = "";

		if (arg >= 86400)
		{
			days = arg / 86400;
			arg = arg - days * 86400;
			wordForm = "дней";
			if (days % 10 == 1)
			{
				wordForm = "день";
			}
			if ((days % 10 >= 2) && (days % 10 <= 4))
			{
				wordForm = "дня";
			}
			if ((days >= 5) && (days <= 20))
			{
				wordForm = "дней";
			}
			res += days + " " + wordForm + " ";
		}
		if (arg >= 3600)
		{
			hours = arg / 3600;
			arg = arg - hours * 3600;
			wordForm = "часов";
			if (hours % 10 == 1)
			{
				wordForm = "час";
			}
			if ((hours % 10 >= 2) && (hours % 10 <= 4))
			{
				wordForm = "часа";
			}
			if ((hours >= 5) && (hours <= 20))
			{
				wordForm = "часов";
			}
			res += hours + " " + wordForm + " ";
		}
		if (arg >= 60)
		{
			minutes = arg / 60;

			wordForm = "минут";
			if (minutes % 10 == 1)
			{
				wordForm = "минуту";
			}
			if ((minutes % 10 >= 2) && (minutes % 10 <= 4))
			{
				wordForm = "минуты";
			}
			if ((minutes >= 5) && (minutes <= 20))
			{
				wordForm = "минут";
			}
			res += minutes + " " + wordForm;
		}
		res = res.trim();
		if (res.equals(""))
		{
			res = "несколько секунд";
		}

		return res;
	}

	public String getEnTimeform(long arg)
	{
		long days = 0;
		long hours = 0;
		long minutes = 0;
		String res = "";
		String wordForm = "";

		if (arg >= 86400)
		{
			days = arg / 86400;
			arg = arg - days * 86400;
			wordForm = "days";
			res += days + " " + wordForm + " ";
		}
		if (arg >= 3600)
		{
			hours = arg / 3600;
			arg = arg - hours * 3600;
			wordForm = "hours";
			res += hours + " " + wordForm + " ";
		}
		if (arg >= 60)
		{
			minutes = arg / 60;
			wordForm = "minutes";
			res += minutes + " " + wordForm;
		}
		res = res.trim();
		if (res.equals(""))
		{
			res = "a few seconds";
		}

		return res;
	}

}
