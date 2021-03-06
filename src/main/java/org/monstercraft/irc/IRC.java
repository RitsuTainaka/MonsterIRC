package org.monstercraft.irc;

import java.net.URL;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.monstercraft.irc.ircplugin.event.EventManager;
import org.monstercraft.irc.plugin.handlers.IRCPluginHandler;
import org.monstercraft.irc.plugin.listeners.IRCListener;
import org.monstercraft.irc.plugin.managers.CommandManager;
import org.monstercraft.irc.plugin.managers.HandleManager;
import org.monstercraft.irc.plugin.managers.HookManager;
import org.monstercraft.irc.plugin.managers.SettingsManager;
import org.monstercraft.irc.plugin.util.Variables;
import org.monstercraft.irc.plugin.wrappers.IRCChannel;
import org.monstercraft.irc.plugin.wrappers.IRCServer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class represents the main plugin. All actions related to the plugin are
 * forwarded by this class
 * 
 * @author Fletch_to_99 <fletchto99@hotmail.com>
 * 
 */
public class IRC extends JavaPlugin implements Runnable {

	private static HandleManager handles = null;
	private static HookManager hooks = null;
	private static CommandManager command = null;
	private static IRCListener listener = null;

	private static IRCServer IRCserver = null;

	private final static Logger logger = Logger.getLogger("MineCraft");

	private static SettingsManager settings = null;
	private Object lock = new Object();
	private static EventManager em = null;
	private static IRCPluginHandler ph = null;

	/**
	 * Enables the plugin.
	 */
	@Override
	public void onEnable() {
		Thread t = new Thread(this);
		t.setDaemon(true);
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();
	}

	/**
	 * Disables the plugin.
	 */
	@Override
	public void onDisable() {
		if (!settings.firstRun()) {
			if (getHandleManager().getIRCHandler() != null) {
				if (getHandleManager().getIRCHandler().isConnected(
						getIRCServer())) {
					for (IRCChannel c : Variables.channels) {
						IRC.getHandleManager().getIRCHandler().leave(c);
					}
					getHandleManager().getIRCHandler().disconnect(
							getIRCServer());
				}
			}
		} else {
			log("Please go edit your config!");
		}
		settings.saveMuted();
		log("Successfully disabled plugin.");
	}

	/**
	 * Handles commands.
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		return getCommandManager().onGameCommand(sender, command, label, args);
	}

	/**
	 * Checks to see if the plugin is the latest version. Thanks to vault for
	 * letting me use their code.
	 * 
	 * @param currentVersion
	 *            The version that is currently running.
	 * @return The latest version
	 */
	private String updateCheck(String currentVersion) {
		String pluginUrlString = "http://dev.bukkit.org/server-mods/monsterirc/files.rss";
		try {
			URL url = new URL(pluginUrlString);
			Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.parse(url.openConnection().getInputStream());
			doc.getDocumentElement().normalize();
			NodeList nodes = doc.getElementsByTagName("item");
			Node firstNode = nodes.item(0);
			if (firstNode.getNodeType() == 1) {
				Element firstElement = (Element) firstNode;
				NodeList firstElementTagName = firstElement
						.getElementsByTagName("title");
				Element firstNameElement = (Element) firstElementTagName
						.item(0);
				NodeList firstNodes = firstNameElement.getChildNodes();
				return firstNodes.item(0).getNodeValue();
			}
		} catch (Exception e) {
			debug(e);
		}
		return currentVersion;
	}

	/**
	 * The plugins settings.
	 * 
	 * @return The settings.
	 */
	protected static SettingsManager getSettingsManager() {
		return settings;
	}

	/**
	 * Logs a message to the console.
	 * 
	 * @param msg
	 *            The message to print.
	 */
	protected static void log(String msg) {
		logger.log(Level.INFO, "[IRC] " + msg);
	}

	/**
	 * Logs debugging messages to the console.
	 * 
	 * @param error
	 *            The message to print.
	 */
	protected static void debug(String error) {
		if (Variables.debug) {
			logger.log(Level.WARNING, "[IRC - Debug] " + error);
		}
	}

	/**
	 * Logs debugging messages to the console.
	 * 
	 * @param error
	 *            The message to print.
	 */
	protected static void debug(Exception error) {
		logger.log(Level.SEVERE, "[IRC - Critical error detected!]");
		error.printStackTrace();
	}

	/**
	 * Stops the server
	 */
	protected void stop() {
		this.getServer()
				.getPluginManager()
				.disablePlugin(
						getServer().getPluginManager().getPlugin("MonsterIRC"));
	}

	/**
	 * The manager that holds the handlers.
	 * 
	 * @return The handlers.
	 */
	public static HandleManager getHandleManager() {
		return handles;
	}

	/**
	 * The manager that creates the hooks with other plugins.
	 * 
	 * @return The hooks.
	 */
	protected static HookManager getHookManager() {
		return hooks;
	}

	/**
	 * The CommandManager that Assigns all the commands.
	 * 
	 * @return The plugins command manager.
	 */
	protected static CommandManager getCommandManager() {
		return command;
	}

	/**
	 * The CommandManager that Assigns all the commands.
	 * 
	 * @return The plugins command manager.
	 */
	public static IRCServer getIRCServer() {
		return IRCserver;
	}

	/**
	 * Fetches the irc channels.
	 * 
	 * @return All of the IRCChannels
	 */
	public static Set<IRCChannel> getChannels() {
		return Variables.channels;
	}

	public static EventManager getEventManager() {
		return em;
	}

	public static IRCPluginHandler getPluginHandler() {
		return ph;
	}

	@Override
	public void run() {
		synchronized (lock) {
			try {
				log("Starting plugin.");
				IRC.settings = new SettingsManager(this);
				IRC.hooks = new HookManager(this);
				IRC.command = new CommandManager(this);
				IRC.listener = new IRCListener(this);
				IRC.IRCserver = new IRCServer(Variables.server, Variables.port,
						Variables.name, Variables.password, Variables.ident,
						Variables.timeout, Variables.limit,
						Variables.connectCommands);
				IRC.handles = new HandleManager(this);
				this.getServer().getPluginManager()
						.registerEvents(listener, this);
//				IRC.em = new EventManager();
//				IRC.em.start();
//				new Configuration();
//				IRC.ph = new IRCPluginHandler(this);
				String currentVersion = getDescription().getVersion();
				String newVersion = updateCheck(currentVersion);
				if (!newVersion.contains(currentVersion)) {
					log(newVersion + " is out! You are running "
							+ currentVersion);
					log("Update MonsterIRC at: http://dev.bukkit.org/server-mods/monsterirc");
				} else {
					log("You are using the latest version of MonsterIRC");
				}
				if (!settings.firstRun()) {
					getHandleManager().getIRCHandler().connect(getIRCServer());
					log("Successfully started up.");
				} else {
					stop();
				}
			} catch (Exception e) {
				debug(e);
			}
		}
	}
}
