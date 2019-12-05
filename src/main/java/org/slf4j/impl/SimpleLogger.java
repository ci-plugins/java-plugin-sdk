//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.slf4j.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.helpers.Util;

public class SimpleLogger extends MarkerIgnoringBase {
    private static final long serialVersionUID = -632788891211436180L;
    private static final String CONFIGURATION_FILE = "simplelogger.properties";
    private static long START_TIME = System.currentTimeMillis();
    private static final Properties SIMPLE_LOGGER_PROPS = new Properties();
    private static final int LOG_LEVEL_TRACE = 0;
    private static final int LOG_LEVEL_DEBUG = 10;
    private static final int LOG_LEVEL_INFO = 20;
    private static final int LOG_LEVEL_WARN = 30;
    private static final int LOG_LEVEL_ERROR = 40;
    private static final int LOG_LEVEL_GROUP_START = 50;
    private static final int LOG_LEVEL_GROUP_END = 51;
    private static boolean INITIALIZED = false;
    private static int DEFAULT_LOG_LEVEL = 20;
    private static boolean SHOW_DATE_TIME = false;
    private static String DATE_TIME_FORMAT_STR = null;
    private static DateFormat DATE_FORMATTER = null;
    private static boolean SHOW_THREAD_NAME = false;
    private static boolean SHOW_LOG_NAME = false;
    private static boolean SHOW_SHORT_LOG_NAME = false;
    private static String LOG_FILE = "System.err";
    private static PrintStream TARGET_STREAM = null;
    private static boolean LEVEL_IN_BRACKETS = false;
    private static String WARN_LEVEL_STRING = "WARN";
    public static final String SYSTEM_PREFIX = "org.slf4j.simpleLogger.";
    public static final String DEFAULT_LOG_LEVEL_KEY = "org.slf4j.simpleLogger.defaultLogLevel";
    public static final String SHOW_DATE_TIME_KEY = "org.slf4j.simpleLogger.showDateTime";
    public static final String DATE_TIME_FORMAT_KEY = "org.slf4j.simpleLogger.dateTimeFormat";
    public static final String SHOW_THREAD_NAME_KEY = "org.slf4j.simpleLogger.showThreadName";
    public static final String SHOW_LOG_NAME_KEY = "org.slf4j.simpleLogger.showLogName";
    public static final String SHOW_SHORT_LOG_NAME_KEY = "org.slf4j.simpleLogger.showShortLogName";
    public static final String LOG_FILE_KEY = "org.slf4j.simpleLogger.logFile";
    public static final String LEVEL_IN_BRACKETS_KEY = "org.slf4j.simpleLogger.levelInBrackets";
    public static final String WARN_LEVEL_STRING_KEY = "org.slf4j.simpleLogger.warnLevelString";
    public static final String LOG_KEY_PREFIX = "org.slf4j.simpleLogger.log.";
    protected int currentLogLevel = 20;
    private transient String shortLogName = null;

    private static String getStringProperty(String name) {
        String prop = null;

        try {
            prop = System.getProperty(name);
        } catch (SecurityException var3) {
        }

        return prop == null ? SIMPLE_LOGGER_PROPS.getProperty(name) : prop;
    }

    private static String getStringProperty(String name, String defaultValue) {
        String prop = getStringProperty(name);
        return prop == null ? defaultValue : prop;
    }

    private static boolean getBooleanProperty(String name, boolean defaultValue) {
        String prop = getStringProperty(name);
        return prop == null ? defaultValue : "true".equalsIgnoreCase(prop);
    }

    static void init() {
        if (!INITIALIZED) {
            INITIALIZED = true;
            loadProperties();
            String defaultLogLevelString = getStringProperty("org.slf4j.simpleLogger.defaultLogLevel", (String)null);
            if (defaultLogLevelString != null) {
                DEFAULT_LOG_LEVEL = stringToLevel(defaultLogLevelString);
            }

            SHOW_LOG_NAME = getBooleanProperty("org.slf4j.simpleLogger.showLogName", SHOW_LOG_NAME);
            SHOW_SHORT_LOG_NAME = getBooleanProperty("org.slf4j.simpleLogger.showShortLogName", SHOW_SHORT_LOG_NAME);
            SHOW_DATE_TIME = getBooleanProperty("org.slf4j.simpleLogger.showDateTime", SHOW_DATE_TIME);
            SHOW_THREAD_NAME = getBooleanProperty("org.slf4j.simpleLogger.showThreadName", SHOW_THREAD_NAME);
            DATE_TIME_FORMAT_STR = getStringProperty("org.slf4j.simpleLogger.dateTimeFormat", DATE_TIME_FORMAT_STR);
            LEVEL_IN_BRACKETS = getBooleanProperty("org.slf4j.simpleLogger.levelInBrackets", LEVEL_IN_BRACKETS);
            WARN_LEVEL_STRING = getStringProperty("org.slf4j.simpleLogger.warnLevelString", WARN_LEVEL_STRING);
            LOG_FILE = getStringProperty("org.slf4j.simpleLogger.logFile", LOG_FILE);
            TARGET_STREAM = computeTargetStream(LOG_FILE);
            if (DATE_TIME_FORMAT_STR != null) {
                try {
                    DATE_FORMATTER = new SimpleDateFormat(DATE_TIME_FORMAT_STR);
                } catch (IllegalArgumentException var2) {
                    Util.report("Bad date format in simplelogger.properties; will output relative time", var2);
                }
            }

        }
    }

    private static PrintStream computeTargetStream(String logFile) {
        if ("System.err".equalsIgnoreCase(logFile)) {
            return System.err;
        } else if ("System.out".equalsIgnoreCase(logFile)) {
            return System.out;
        } else {
            try {
                FileOutputStream fos = new FileOutputStream(logFile);
                PrintStream printStream = new PrintStream(fos);
                return printStream;
            } catch (FileNotFoundException var3) {
                Util.report("Could not open [" + logFile + "]. Defaulting to System.err", var3);
                return System.err;
            }
        }
    }

    private static void loadProperties() {
        InputStream in = (InputStream)AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
            public InputStream run() {
                ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
                return threadCL != null ? threadCL.getResourceAsStream("simplelogger.properties") : ClassLoader.getSystemResourceAsStream("simplelogger.properties");
            }
        });
        if (null != in) {
            try {
                SIMPLE_LOGGER_PROPS.load(in);
                in.close();
            } catch (IOException var2) {
            }
        }

    }

    SimpleLogger(String name) {
        this.name = name;
        String levelString = this.recursivelyComputeLevelString();
        if (levelString != null) {
            this.currentLogLevel = stringToLevel(levelString);
        } else {
            this.currentLogLevel = DEFAULT_LOG_LEVEL;
        }

    }

    String recursivelyComputeLevelString() {
        String tempName = this.name;
        String levelString = null;

        for(int indexOfLastDot = tempName.length(); levelString == null && indexOfLastDot > -1; indexOfLastDot = String.valueOf(tempName).lastIndexOf(".")) {
            tempName = tempName.substring(0, indexOfLastDot);
            levelString = getStringProperty("org.slf4j.simpleLogger.log." + tempName, (String)null);
        }

        return levelString;
    }

    private static int stringToLevel(String levelStr) {
        if ("trace".equalsIgnoreCase(levelStr)) {
            return 0;
        } else if ("debug".equalsIgnoreCase(levelStr)) {
            return 10;
        } else if ("info".equalsIgnoreCase(levelStr)) {
            return 20;
        } else if ("warn".equalsIgnoreCase(levelStr)) {
            return 30;
        } else {
            return "error".equalsIgnoreCase(levelStr) ? 40 : 20;
        }
    }

    private void log(int level, String message, Throwable t) {
        if (this.isLevelEnabled(level)) {
            StringBuilder buf = new StringBuilder(32);
            if (SHOW_DATE_TIME) {
                if (DATE_FORMATTER != null) {
                    buf.append(this.getFormattedDate());
                    buf.append(' ');
                } else {
                    buf.append(System.currentTimeMillis() - START_TIME);
                    buf.append(' ');
                }
            }

            if (SHOW_THREAD_NAME) {
                buf.append('[');
                buf.append(Thread.currentThread().getName());
                buf.append("] ");
            }

            if (LEVEL_IN_BRACKETS) {
                buf.append('[');
            }

            switch(level) {
                case 0:
                    buf.append("TRACE");
                    break;
                case 10:
                    buf.append("##[");
                    buf.append("debug");
                    buf.append("] ");
                    break;
                case 20:
                    buf.append("##[");
                    buf.append("info");
                    buf.append("] ");
                    break;
                case 30:
                    buf.append("##[");
                    buf.append("warning");
                    buf.append("] ");
                    break;
                case 40:
                    buf.append("##[");
                    buf.append("error");
                    buf.append("] ");
                    break;
                case 50:
                    buf.append("##[");
                    buf.append("group");
                    buf.append("] ");
                    break;
                case 51:
                    buf.append("##[");
                    buf.append("endgroup");
                    buf.append("] ");
            }

            if (LEVEL_IN_BRACKETS) {
                buf.append(']');
            }

            if (SHOW_SHORT_LOG_NAME) {
                if (this.shortLogName == null) {
                    this.shortLogName = this.computeShortName();
                }

                buf.append(String.valueOf(this.shortLogName)).append(" - ");
            } else if (SHOW_LOG_NAME) {
                buf.append(String.valueOf(this.name)).append(" - ");
            }

            buf.append(message);
            this.write(buf, t);
        }
    }

    void write(StringBuilder buf, Throwable t) {
        TARGET_STREAM.println(buf.toString());
        if (t != null) {
            t.printStackTrace(TARGET_STREAM);
        }

        TARGET_STREAM.flush();
    }

    private String getFormattedDate() {
        Date now = new Date();
        synchronized(DATE_FORMATTER) {
            String dateText = DATE_FORMATTER.format(now);
            return dateText;
        }
    }

    private String computeShortName() {
        return this.name.substring(this.name.lastIndexOf(".") + 1);
    }

    private void formatAndLog(int level, String format, Object arg1, Object arg2) {
        if (this.isLevelEnabled(level)) {
            FormattingTuple tp = MessageFormatter.format(format, arg1, arg2);
            this.log(level, tp.getMessage(), tp.getThrowable());
        }
    }

    private void formatAndLog(int level, String format, Object... arguments) {
        if (this.isLevelEnabled(level)) {
            FormattingTuple tp = MessageFormatter.arrayFormat(format, arguments);
            this.log(level, tp.getMessage(), tp.getThrowable());
        }
    }

    protected boolean isLevelEnabled(int logLevel) {
        return logLevel >= this.currentLogLevel;
    }

    public boolean isTraceEnabled() {
        return this.isLevelEnabled(0);
    }

    public void trace(String msg) {
        this.log(0, msg, (Throwable)null);
    }

    public void trace(String format, Object param1) {
        this.formatAndLog(0, format, param1, (Object)null);
    }

    public void trace(String format, Object param1, Object param2) {
        this.formatAndLog(0, format, param1, param2);
    }

    public void trace(String format, Object... argArray) {
        this.formatAndLog(0, format, argArray);
    }

    public void trace(String msg, Throwable t) {
        this.log(0, msg, t);
    }

    public boolean isDebugEnabled() {
        return this.isLevelEnabled(10);
    }

    public void debug(String msg) {
        this.log(10, msg, (Throwable)null);
    }

    public void debug(String format, Object param1) {
        this.formatAndLog(10, format, param1, (Object)null);
    }

    public void debug(String format, Object param1, Object param2) {
        this.formatAndLog(10, format, param1, param2);
    }

    public void debug(String format, Object... argArray) {
        this.formatAndLog(10, format, argArray);
    }

    public void debug(String msg, Throwable t) {
        this.log(10, msg, t);
    }

    public boolean isInfoEnabled() {
        return this.isLevelEnabled(20);
    }

    public void info(String msg) {
        this.log(20, msg, (Throwable)null);
    }

    public void info(String format, Object arg) {
        this.formatAndLog(20, format, arg, (Object)null);
    }

    public void info(String format, Object arg1, Object arg2) {
        this.formatAndLog(20, format, arg1, arg2);
    }

    public void info(String format, Object... argArray) {
        this.formatAndLog(20, format, argArray);
    }

    public void info(String msg, Throwable t) {
        this.log(20, msg, t);
    }

    public boolean isWarnEnabled() {
        return this.isLevelEnabled(30);
    }

    public void warn(String msg) {
        this.log(30, msg, (Throwable)null);
    }

    public void warn(String format, Object arg) {
        this.formatAndLog(30, format, arg, (Object)null);
    }

    public void warn(String format, Object arg1, Object arg2) {
        this.formatAndLog(30, format, arg1, arg2);
    }

    public void warn(String format, Object... argArray) {
        this.formatAndLog(30, format, argArray);
    }

    public void warn(String msg, Throwable t) {
        this.log(30, msg, t);
    }

    public boolean isErrorEnabled() {
        return this.isLevelEnabled(40);
    }

    public void error(String msg) {
        this.log(40, msg, (Throwable)null);
    }

    public void error(String format, Object arg) {
        this.formatAndLog(40, format, arg, (Object)null);
    }

    public void error(String format, Object arg1, Object arg2) {
        this.formatAndLog(40, format, arg1, arg2);
    }

    public void error(String format, Object... argArray) {
        this.formatAndLog(40, format, argArray);
    }

    public void error(String msg, Throwable t) {
        this.log(40, msg, t);
    }

    public void groupStart(String startTitle){
        this.log(50, startTitle, (Throwable)null);
    }

    public void groupEnd(String endTitle){
        this.log(51, endTitle, (Throwable)null);
    }

    public void log(LoggingEvent event) {
        int levelInt = event.getLevel().toInt();
        if (this.isLevelEnabled(levelInt)) {
            FormattingTuple tp = MessageFormatter.arrayFormat(event.getMessage(), event.getArgumentArray(), event.getThrowable());
            this.log(levelInt, tp.getMessage(), event.getThrowable());
        }
    }
}
