package com.android.util.system;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import android.text.TextUtils;
import android.util.Log;

public class Logger {
    private static final String defaultTag = "Logger";
    public static final boolean DEBUG = true;
    private static RandomAccessFile randomFile;
    private static StringBuilder fLogBuf;
    private static boolean isfLogBuf = true;
    private static Vector<OnLogAppender> onLogAppenders;

    public static class LogContext {
        String fileName;
        int lineNumber;

        public String getTag() {
            return fileName + " (" + lineNumber + ")";
        }
    }

    /**
     * 日志类别
     */
    public static enum LogType {
        // Log.v
        VERBOSE,
        // Log.d
        DEBUG,
        // Log.i
        INFO,
        // Log.w
        WARNING,
        // Log.e
        ERROR,
        // Log.f
        FILE;
    }

    /**
     * 记录一条Log.v
     * 
     * @param msg
     */
    public static void v(String msg) {
        log(LogType.VERBOSE, null, msg);
    }

    /**
     * 记录一条Log.d
     * 
     * @param msg
     */
    public static void d(String msg) {
        log(LogType.DEBUG, null, msg);
    }

    /**
     * 记录一条信息
     * 
     * @param msg
     */
    public static void i(String msg) {
        log(LogType.INFO, null, msg);
    }

    /**
     * 记录一条警告日志
     * 
     * @param msg
     */
    public static void w(String msg) {
        log(LogType.WARNING, null, msg);
    }

    /**
     * 记录一条警告异常
     * 
     * @param throwable
     */
    public static void w(Throwable throwable) {
        log(LogType.WARNING, null, throwable);
    }

    /**
     * 记录一条警告异常
     * 
     * @param throwable
     */
    public static void w(String msg, Throwable throwable) {
        log(LogType.WARNING, msg, throwable);
    }

    /**
     * 记录一条错误信息
     * 
     * @param msg
     */
    public static void e(String msg) {
        log(LogType.ERROR, null, msg);
    }

    /**
     * 记录一条异常日志
     * 
     * @param throwable
     */
    public static void e(Throwable throwable) {
        log(LogType.ERROR, null, throwable);
    }

    /**
     * 记录一条异常日志
     * 
     * @param throwable
     */
    public static void e(String msg, Throwable throwable) {
        log(LogType.ERROR, msg, throwable);
    }

    /**
     * 记录一条异常日志
     * 
     * @param throwable
     */
    public static void f(String msg) {
        long ftime = System.currentTimeMillis();
        log(LogType.FILE, null, msg + ":t=" + DateUtil.getHMS(ftime) + ":"
                + ftime + "\n");
    }

    /**
     * 记录一条异常日志
     * 
     * @param type
     * @param throwable
     */
    private static void log(LogType type, String msg, Throwable throwable) {
        if (!TextUtils.isEmpty(msg)) {
            LogContext context = getLogStatus();
            log(type, context, msg);
        }

        if (throwable != null) {
            if (true) {
                throwable.printStackTrace();
                return;
            }
        }
    }

    /**
     * 记录一条日志
     * 
     * @param type
     *            日志的类别
     * @param context
     *            日志上下文
     * @param msg
     *            日志内容
     */
    private static void log(LogType type, LogContext context, String msg) {
        if (msg == null) {
            msg = "null";
        }
        if (context == null) {
            context = getLogStatus();
        }
        if (DEBUG) {
            switch (type) {
            case DEBUG: {
                Log.d(context.getTag(), msg);
                break;
            }
            case ERROR: {
                Log.e(context.getTag(), msg);
                break;
            }
            case INFO: {
                Log.i(context.getTag(), msg);
                break;
            }
            case VERBOSE: {
                Log.v(context.getTag(), msg);
                break;
            }
            case WARNING: {
                Log.w(context.getTag(), msg);
                break;
            }
            }
        }
        if (LogType.FILE == type) {
            Log.d(context.getTag(), msg);
            appendFile(msg);
            notifyListeners(msg);
        }
    }

    private static LogContext getLogStatus() {
        LogContext logStatus = new LogContext();
        StackTraceElement[] stes = new Throwable().getStackTrace();
        if (stes != null && stes.length >= 4) {
            StackTraceElement caller = stes[3];
            logStatus.fileName = caller.getFileName();
            logStatus.lineNumber = caller.getLineNumber();
        } else {
            logStatus.fileName = defaultTag;
        }
        return logStatus;
    }

    private synchronized static void appendFile(String msg) {
        initLogFile();
        if (randomFile != null) {
            try {
                randomFile.seek(randomFile.length());
                randomFile.writeUTF(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (DEBUG && fLogBuf != null && isfLogBuf) {
            fLogBuf.append(msg);
        }
    }

    private static synchronized void initLogFile() {
        if (randomFile == null) {
            File f = AppHelper.getAppLog();
            try {
                if (!f.exists()) {
                    f.createNewFile();
                } else {
                    // 大于500KB
                    if (f.length() > 500 * 1024) {
                        f.renameTo(AppHelper.getAppBackLog());
                        f = AppHelper.getAppLog();
                        f.createNewFile();
                    }
                }
                fLogBuf = new StringBuilder();
                randomFile = new RandomAccessFile(f, "rw");
                isfLogBuf = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized static void addOnLogAppender(OnLogAppender listener) {
        if (onLogAppenders == null) {
            onLogAppenders = new Vector<Logger.OnLogAppender>();
        }
        if (!onLogAppenders.contains(listener)) {
            onLogAppenders.add(listener);
        }
    }

    public synchronized static void rmvOnLogAppender(OnLogAppender listener) {
        onLogAppenders.remove(listener);
    }

    private synchronized static void notifyListeners(String msg) {
        if (DEBUG && onLogAppenders != null) {
            try {
                for (OnLogAppender listener : onLogAppenders) {
                    try {
                        listener.onLogAppend(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized static String readFileLog() {
        initLogFile();
        if (fLogBuf != null) {
            return fLogBuf.toString();
        } else {
            return "";
        }
    }

    /** 设置不用文件日志缓存 */
    public synchronized static void setNoFLogBuf() {
        isfLogBuf = false;
    }

    public synchronized static void destory() {
        if (randomFile != null) {
            try {
                randomFile.close();
                randomFile = null;
                fLogBuf = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (onLogAppenders != null) {
            onLogAppenders.clear();
        }
    }

    public interface OnLogAppender {
        public void onLogAppend(String log);
    }
}