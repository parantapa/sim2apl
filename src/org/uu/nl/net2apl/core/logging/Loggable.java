package org.uu.nl.net2apl.core.logging;

import java.util.logging.Level;

/**
 * 
 * @author Jurian Baas
 *
 */
public abstract class Loggable {
	
	/**
	 * Log a message
	 * @param c The class that generated the message
	 * @param level The severity level of the message
	 * @param message The object to log. <code>toString()</code> will be called.
	 */
	public abstract void log(Class<?> c, Level level, Object message);
	
	/**
	 * Shorthand method for logging the most common INFO level message
	 * @param c The class that generated the message
	 * @param message The object to log. <code>toString()</code> will be called.
	 */
	public void log(Class<?> c, Object message) {
		log(c, Level.INFO, message);
	}
	
	/**
	 * Shorthand method for logging an exception.
	 * @param c The class that generated the message
	 * @param ex The exception to log
	 */
	public void log(Class<?> c, Exception ex) {
		log(c, Level.SEVERE, ex.getMessage());
		for(StackTraceElement el : ex.getStackTrace()) {
			log(c, Level.SEVERE,  "\t| " + el.toString());
		}
	}
}
