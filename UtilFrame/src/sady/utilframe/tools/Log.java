package sady.utilframe.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
	private static Log logger;
//	private static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private FileTools ft;
	private Level level = Level.WARN;
	
	public static enum Level {
		INFO(1),
		DEBUG(2),
		WARN(3),
		ERROR(4),
		FATAL(5);
		
		int l;
		private Level(int l) {
			this.l = l;
		}
	}
	
	private static void print(Level lvl, String value, Throwable t) {
		Log log = getLog();
		try {
			if (lvl.l >= log.level.l) {
				String line = formatter.format(new Date()) + " " + StringTools.rPad(lvl.toString(), 6, ' ');
				System.out.println(line + value);
				log.ft.writeLine(line + value);
				
				if (t != null) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					t.printStackTrace(pw);
					sw.toString();

					System.out.println(sw.toString());
					log.ft.writeLine(sw.toString());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void debug(String debug) {
		print(Level.DEBUG, debug, null);
	}

	public static void info(String info) {
		print(Level.INFO, info, null);
	}
	
    public static void warn(String warn) {
    	print(Level.WARN, warn, null);
    }
	
	public static void error(String error) {
		print(Level.ERROR, error, null);
	}
	
	public static void fatal(String fatal) {
		print(Level.FATAL, fatal, null);
	}

	public static void debug(String debug, Throwable throwable) {
		print(Level.DEBUG, debug, throwable);
	}

	public static void info(String info, Throwable throwable) {
		print(Level.INFO, info, throwable);
	}

	public static void warn(String warn, Throwable throwable) {
		print(Level.WARN, warn, throwable);
	}
	
	public static void error(String error, Throwable throwable) {
		print(Level.ERROR, error, throwable);
	}
	
	public static void fatal(String fatal, Throwable throwable) {
		print(Level.FATAL, fatal, throwable);
	}

	private static Log getLog() {
		if (logger == null) {
			logger = new Log();
			try {
				logger.ft = new FileTools(null, "log.log", 1, true);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		
		return logger;
	}
	public static void setLevel(Level level) {
		getLog().level = level;
	}

}




/*
public class Log {
	private static Logger logger;
	private static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	public static void debug(String debug) {
		Log.getLog().debug("(" +formatter.format(new Date()) + ") " + debug);
	}

	public static void info(String info) {
		Log.getLog().info("(" +formatter.format(new Date()) + ") " + info);
	}
	
    public static void warn(String warn) {
        Log.getLog().warn("(" +formatter.format(new Date()) + ") " + warn);
    }
	
	public static void error(String error) {
		Log.getLog().error("(" +formatter.format(new Date()) + ") " + error);
	}
	
	public static void fatal(String fatal) {
		Log.getLog().fatal("(" +formatter.format(new Date()) + ") " + fatal);
	}

	public static void debug(String debug, Throwable throwable) {
		Log.getLog().debug("(" +formatter.format(new Date()) + ") " + debug, throwable);
	}

	public static void info(String info, Throwable throwable) {
		Log.getLog().info("(" +formatter.format(new Date()) + ") " + info, throwable);
	}
	
	public static void error(String error, Throwable throwable) {
		Log.getLog().error("(" +formatter.format(new Date()) + ") " + error, throwable);
	}
	
	public static void fatal(String fatal, Throwable throwable) {
		Log.getLog().fatal("(" +formatter.format(new Date()) + ") " + fatal, throwable);
	}

	private static Logger getLog() {
		if (logger == null) {
			Log.logger = Logger.getLogger("");
			BasicConfigurator.configure();
			try {
				Appender fileAppender = new FileAppender(
						             new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN), "log.log");
				Log.logger.addAppender(fileAppender);
			} catch (IOException e) {
				e.printStackTrace();
			}
			 
			 Log.logger.setLevel(Level.ERROR);
		}
		return Log.logger;
	}
	public static void setLevel(Level level) {
		Log.getLog().setLevel(level);
	}

}
*/