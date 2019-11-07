package co.insecurity.gradle.repository_mirrors


import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.slf4j.Marker

class ColorizedLogger implements Logger {

    public enum ANSIColor {
        BRIGHT_GREEN('\033[0;92m'),
        YELLOW('\033[0;33m'),
        BRIGHT_RED('\033[0;91m'),
        RESET('\033[0m');

        private final String escapeCode;

        ANSIColor(final String escapeCode) {
            this.escapeCode = escapeCode;
        }

        String escapeCode() {
            return this.escapeCode;
        }

        @Override
        String toString() {
            return this.escapeCode()
        }
    }

    private final Logger log;
    private final LogLevel defaultLevel;

    private ColorizedLogger(Class c) {
        this(c, LogLevel.INFO)
    }

    private ColorizedLogger(Class c, LogLevel defaultLevel) {
        this.log = Logging.getLogger(c)
        this.defaultLevel = defaultLevel
    }

    public static ColorizedLogger getLogger(Class c, LogLevel defaultLevel = LogLevel.INFO) {
        return new ColorizedLogger(c, defaultLevel)
    }

    String colorize(LogLevel level, String message) {
        String colorStart = ''
        String colorEnd = ANSIColor.RESET.toString()
        switch(level) {
            case LogLevel.INFO:
                colorStart = ANSIColor.BRIGHT_GREEN.toString()
                break
            case LogLevel.WARN:
                colorStart = ANSIColor.YELLOW.toString()
                break
            case LogLevel.ERROR:
                colorStart = ANSIColor.BRIGHT_RED.toString()
                break
            default:
                colorEnd = ''
                break

        }
        message = "${colorStart}(${level})${colorEnd} ${this.log.getName()} - ${message}"
        return message
    }

    @Override
    String getName() {
        this.log.getName()
    }

    @Override
    boolean isQuietEnabled() {
        return false
    }

    @Override
    void quiet(String message) {

    }

    @Override
    void quiet(String message, Object... objects) {

    }

    @Override
    void quiet(String message, Throwable throwable) {

    }

    @Override
    boolean isLifecycleEnabled() {
        return this.isEnabled(LogLevel.LIFECYCLE)
    }

    @Override
    void lifecycle(String message) {
        this.log(LogLevel.LIFECYCLE, message)
    }

    @Override
    void lifecycle(String message, Object... objects) {
        this.log(LogLevel.LIFECYCLE, message, objects)
    }

    @Override
    void lifecycle(String message, Throwable throwable) {
        this.log(LogLevel.LIFECYCLE, message, throwable)
    }

    @Override
    boolean isTraceEnabled() {
        return this.log.isTraceEnabled()
    }

    @Override
    void trace(String s) {
        this.log.trace(this.colorize(null, s))
    }

    @Override
    void trace(String s, Object o) {
        this.log.trace(this.colorize(null, s), o)
    }

    @Override
    void trace(String s, Object o, Object o1) {
        this.log.trace(this.colorize(null, s), o, o1)
    }

    @Override
    void trace(String s, Object... objects) {
        this.log.trace(this.colorize(null, s), objects)
    }

    @Override
    void trace(String s, Throwable throwable) {
        this.trace(s, throwable)
    }

    @Override
    boolean isTraceEnabled(Marker marker) {
        return this.log.isTraceEnabled(marker)
    }

    @Override
    void trace(Marker marker, String s) {
        this.log.trace(marker, this.colorize(null, s))
    }

    @Override
    void trace(Marker marker, String s, Object o) {
        this.log.trace(marker, this.colorize(null, s), o)
    }

    @Override
    void trace(Marker marker, String s, Object o, Object o1) {
        this.log.trace(marker, this.colorize(null, s),o, o1)
    }

    @Override
    void trace(Marker marker, String s, Object... objects) {
        this.log.trace(marker, this.colorize(null, s), objects)
    }

    @Override
    void trace(Marker marker, String s, Throwable throwable) {
        this.log.trace(marker, this.colorize(null, s), throwable)
    }

    @Override
    boolean isDebugEnabled() {
        return this.isEnabled(LogLevel.DEBUG)
    }

    @Override
    void debug(String s) {
        this.log(LogLevel.DEBUG, s)
    }

    @Override
    void debug(String s, Object o) {
        this.log(LogLevel.DEBUG, s, o)
    }

    @Override
    void debug(String s, Object o, Object o1) {
        this.log(LogLevel.DEBUG, s, o, o1)
    }

    @Override
    void debug(String s, Object... objects) {
        this.log(LogLevel.DEBUG, s, objects)
    }

    @Override
    void debug(String s, Throwable throwable) {
        this.log(LogLevel.DEBUG, s, throwable)
    }

    @Override
    boolean isDebugEnabled(Marker marker) {
        return this.log.isDebugEnabled(marker)
    }

    @Override
    void debug(Marker marker, String s) {
        this.log.debug(marker, this.colorize(null, s))
    }

    @Override
    void debug(Marker marker, String s, Object o) {
        this.log.debug(marker, this.colorize(null, s), o)
    }

    @Override
    void debug(Marker marker, String s, Object o, Object o1) {
        this.log.debug(marker, this.colorize(null, s),o, o1)
    }

    @Override
    void debug(Marker marker, String s, Object... objects) {
        this.log.debug(marker, this.colorize(null, s), objects)
    }

    @Override
    void debug(Marker marker, String s, Throwable throwable) {
        this.log.debug(marker, this.colorize(null, s), throwable)
    }

    @Override
    boolean isInfoEnabled() {
        return this.isEnabled(LogLevel.INFO)
    }

    @Override
    void info(String s) {
        this.log(LogLevel.INFO, s)
    }

    @Override
    void info(String s, Object o) {
        this.log(LogLevel.INFO, s, o)
    }

    @Override
    void info(String s, Object o, Object o1) {
        this.log(LogLevel.INFO, s, o, o1)
    }

    @Override
    void info(String s, Object... objects) {
        this.log(LogLevel.INFO, s, objects)
    }

    @Override
    void info(String s, Throwable throwable) {
        this.log(LogLevel.INFO, s, throwable)
    }

    @Override
    boolean isInfoEnabled(Marker marker) {
        return this.log.isInfoEnabled(marker)
    }

    @Override
    void info(Marker marker, String s) {
        this.log.info(marker, this.colorize(null, s))
    }

    @Override
    void info(Marker marker, String s, Object o) {
        this.log.info(marker, this.colorize(null, s), o)
    }

    @Override
    void info(Marker marker, String s, Object o, Object o1) {
        this.log.info(marker, this.colorize(null, s),o, o1)
    }

    @Override
    void info(Marker marker, String s, Object... objects) {
        this.log.info(marker, this.colorize(null, s), objects)
    }

    @Override
    void info(Marker marker, String s, Throwable throwable) {
        this.log.info(marker, this.colorize(null, s), throwable)
    }

    @Override
    boolean isWarnEnabled() {
        return this.isEnabled(LogLevel.WARN)
    }

    @Override
    void warn(String s) {
        this.log(LogLevel.WARN, s)
    }

    @Override
    void warn(String s, Object o) {
        this.log(LogLevel.WARN, s, o)
    }

    @Override
    void warn(String s, Object o, Object o1) {
        this.log(LogLevel.WARN, s, o, o1)
    }

    @Override
    void warn(String s, Object... objects) {
        this.log(LogLevel.WARN, s, objects)
    }

    @Override
    void warn(String s, Throwable throwable) {
        this.log(LogLevel.WARN, s, throwable)
    }

    @Override
    boolean isWarnEnabled(Marker marker) {
        return this.log.isWarnEnabled(marker)
    }

    @Override
    void warn(Marker marker, String s) {
        this.log.warn(marker, this.colorize(null, s))
    }

    @Override
    void warn(Marker marker, String s, Object o) {
        this.log.warn(marker, this.colorize(null, s), o)
    }

    @Override
    void warn(Marker marker, String s, Object o, Object o1) {
        this.log.warn(marker, this.colorize(null, s),o, o1)
    }

    @Override
    void warn(Marker marker, String s, Object... objects) {
        this.log.warn(marker, this.colorize(null, s), objects)
    }

    @Override
    void warn(Marker marker, String s, Throwable throwable) {
        this.log.warn(marker, this.colorize(null, s), throwable)
    }

    @Override
    boolean isErrorEnabled() {
        return this.isEnabled(LogLevel.ERROR)
    }

    @Override
    void error(String s) {
        this.log(LogLevel.ERROR, s)
    }

    @Override
    void error(String s, Object o) {
        this.log(LogLevel.ERROR, s, o)
    }

    @Override
    void error(String s, Object o, Object o1) {
        this.log(LogLevel.ERROR, s, o, o1)
    }

    @Override
    void error(String s, Object... objects) {
        this.log(LogLevel.ERROR, s, objects)
    }

    @Override
    void error(String s, Throwable throwable) {
        this.log(LogLevel.ERROR, s, throwable)
    }

    @Override
    boolean isErrorEnabled(Marker marker) {
        return this.log.isErrorEnabled(marker)
    }

    @Override
    void error(Marker marker, String s) {
        this.log.error(marker, this.colorize(null, s))
    }

    @Override
    void error(Marker marker, String s, Object o) {
        this.log.error(marker, this.colorize(null, s), o)
    }

    @Override
    void error(Marker marker, String s, Object o, Object o1) {
        this.log.error(marker, this.colorize(null, s),o, o1)
    }

    @Override
    void error(Marker marker, String s, Object... objects) {
        this.log.error(marker, this.colorize(null, s), objects)
    }

    @Override
    void error(Marker marker, String s, Throwable throwable) {
        this.log.error(marker, this.colorize(null, s), throwable)
    }

    @Override
    boolean isEnabled(LogLevel level) {
        return this.log.isEnabled(level)
    }

    @Override
    void log(LogLevel level, String message) {
        this.log.log(level, this.colorize(level, message))
    }

    @Override
    void log(LogLevel level, String message, Object... objects) {
        this.log.log(level, this.colorize(level, message), objects)
    }

    @Override
    void log(LogLevel level, String message, Throwable throwable) {
        this.log.log(level, this.colorize(level, message), throwable)
    }
}
