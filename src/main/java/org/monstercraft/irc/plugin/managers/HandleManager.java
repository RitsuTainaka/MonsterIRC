package org.monstercraft.irc.plugin.managers;

import org.monstercraft.irc.IRC;
import org.monstercraft.irc.plugin.handlers.IRCHandler;
import org.monstercraft.irc.plugin.handlers.IRCPermissionsHandler;
import org.monstercraft.irc.plugin.hooks.VaultPermissionsHook;

/**
 * This class contains all of the plugins handles.
 * 
 * @author fletch_to_99 <fletchto99@hotmail.com>
 * 
 */
public class HandleManager extends IRC {

	private IRCHandler irc = null;
	private IRCPermissionsHandler perms = null;

	/**
	 * Creates an instance of the Handle class.
	 * 
	 * @param plugin
	 *            The parent plugin.
	 */
	public HandleManager(final IRC plugin) {
		irc = new IRCHandler(plugin);
		perms = new IRCPermissionsHandler(getHookManager().getPermissionsHook()
				.getHook());
	}

	/**
	 * Fetches the IRCHandler.
	 * 
	 * @return The IRCHandler.
	 */
	public IRCHandler getIRCHandler() {
		return irc;
	}

	/**
	 * Fetches the permission handler.
	 * 
	 * @return The PermissionsHandler.
	 */
	public IRCPermissionsHandler getPermissionsHandler() {
		return perms;
	}

	/**
	 * Sets the PermissionsHandler.
	 * 
	 * @param hook
	 *            The hook into Permissions.
	 * @return The new permissions Handler.
	 */
	public IRCPermissionsHandler setPermissionsHandler(
			final VaultPermissionsHook hook) {
		if (hook != null) {
			if (perms.isEnabled()) {
				return perms = new IRCPermissionsHandler(hook.getHook());
			}
		}
		return perms;
	}

}
