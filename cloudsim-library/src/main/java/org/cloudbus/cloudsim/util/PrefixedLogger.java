package org.cloudbus.cloudsim.util;

import org.cloudbus.cloudsim.core.CloudSim;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/** PrefixedLogger wraps a {@link org.slf4j.Logger} by prepending a prefix to logged messages.
 *
 * The prefix is a String that may optionally include an SLF4J placeholder `{}`. The actual prefix
 * added to the messages is built from a prefix Object whose String representation is inserted in
 * lieu of the placeholder, using SLF4J's formatting mechanisms.
 *
 * The prefix is built only once on PrefixedLogger construction, and re-built on prefix or prefix
 * Object change; however one can override {@link #getFormattedPrefix()} to change the prefix on a
 * per-message basis.
 */
public class PrefixedLogger implements Logger {
    private Logger logger;
    private Object prefixObject = null;
    private String prefix;
    private boolean printSimulationDate;

    public PrefixedLogger(Logger logger, String prefix) {
        this(logger, prefix, true);
    }

    /** Create a new PrefixedLogger with a fixed prefix.
     *
     * If prefix contains an SLF4J placeholder `{}`, it will not get replaced not neutralized, so
     * this will result in unexpected behavior until the prefix Object is set, just like multiple
     * placeholders with {@link #PrefixedLogger(Logger, String, Object, boolean)}.
     *
     * @param logger wrapped logger
     * @param prefix prefix to be prepended to all logged messages
     * @param printSimulationDate whether to print simulation date when logging
     */
    public PrefixedLogger(Logger logger, String prefix, boolean printSimulationDate) {
        setDelegate(logger);
        setPrefix(prefix);
        enablePrintingSimulationDate(printSimulationDate);
    }

    public PrefixedLogger(Logger logger, String prefix, Object prefixObject) {
        this(logger, prefix, prefixObject, true);
    }

    /** Create a new PrefixedLogger with a prefix Object.
     *
     * prefix is expected to include exactly one SLF4J placeholder anchor `{}`, which is replaced by
     * the String representation of prefixObject using SLF4J formatting mechanisms. More than one
     * placeholder will result in unexpected behavior.
     *
     * printSimulationDate is an option to print the simulation date at the time of message logging.
     * The date is added before the prefix.
     *
     * @param logger wrapped logger
     * @param prefix prefix to be prepended to all logged messages
     * @param prefixObject prefixObject whose String representation is included in the prepended prefix
     * @param printSimulationDate whether to print simulation date when logging
     */
    public PrefixedLogger(Logger logger, String prefix, Object prefixObject, boolean printSimulationDate) {
        setDelegate(logger);
        setPrefix(prefix, prefixObject);
        enablePrintingSimulationDate(printSimulationDate);
    }

    @Override
    public String getName() {
        return delegate().getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return delegate().isTraceEnabled();
    }

    @Override
    public void trace(String s) {
        delegate().trace(this.getFormattedPrefix() + s);
    }

    @Override
    public void trace(String s, Object o) {
        delegate().trace(this.getFormattedPrefix() + s, o);
    }

    @Override
    public void trace(String s, Object o, Object o1) {
        delegate().trace(this.getFormattedPrefix() + s, o, o1);
    }

    @Override
    public void trace(String s, Object... objects) {
        delegate().trace(this.getFormattedPrefix() + s, objects);
    }

    @Override
    public void trace(String s, Throwable throwable) {
        delegate().trace(this.getFormattedPrefix() + s, throwable);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return delegate().isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String s) {
        delegate().trace(marker, this.getFormattedPrefix() + s);
    }

    @Override
    public void trace(Marker marker, String s, Object o) {
        delegate().trace(marker, this.getFormattedPrefix() + s, o);
    }

    @Override
    public void trace(Marker marker, String s, Object o, Object o1) {
        delegate().trace(marker, this.getFormattedPrefix() + s, o, o1);
    }

    @Override
    public void trace(Marker marker, String s, Object... objects) {
        delegate().trace(marker, this.getFormattedPrefix() + s, objects);
    }
    
    @Override
    public void trace(Marker marker, String s, Throwable throwable) {
        delegate().trace(marker, this.getFormattedPrefix() + s, throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate().isDebugEnabled();
    }

    @Override
    public void debug(String s) {
        delegate().debug(this.getFormattedPrefix() + s);
    }

    @Override
    public void debug(String s, Object o) {
        delegate().debug(this.getFormattedPrefix() + s, o);
    }

    @Override
    public void debug(String s, Object o, Object o1) {
        delegate().debug(this.getFormattedPrefix() + s, o, o1);
    }

    @Override
    public void debug(String s, Object... objects) {
        delegate().debug(this.getFormattedPrefix() + s, objects);
    }

    @Override
    public void debug(String s, Throwable throwable) {
        delegate().debug(this.getFormattedPrefix() + s, throwable);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return delegate().isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String s) {
        delegate().debug(marker, this.getFormattedPrefix() + s);
    }

    @Override
    public void debug(Marker marker, String s, Object o) {
        delegate().debug(marker, this.getFormattedPrefix() + s, o);
    }

    @Override
    public void debug(Marker marker, String s, Object o, Object o1) {
        delegate().debug(marker, this.getFormattedPrefix() + s, o, o1);
    }

    @Override
    public void debug(Marker marker, String s, Object... objects) {
        delegate().debug(marker, this.getFormattedPrefix() + s, objects);
    }

    @Override
    public void debug(Marker marker, String s, Throwable throwable) {
        delegate().debug(marker, this.getFormattedPrefix() + s, throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate().isInfoEnabled();
    }

    @Override
    public void info(String s) {
        delegate().info(this.getFormattedPrefix() + s);
    }

    @Override
    public void info(String s, Object o) {
        delegate().info(this.getFormattedPrefix() + s, o);
    }

    @Override
    public void info(String s, Object o, Object o1) {
        delegate().info(this.getFormattedPrefix() + s, o, o1);
    }

    @Override
    public void info(String s, Object... objects) {
        delegate().info(this.getFormattedPrefix() + s, objects);
    }

    @Override
    public void info(String s, Throwable throwable) {
        delegate().info(this.getFormattedPrefix() + s, throwable);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return delegate().isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String s) {
        delegate().info(marker, this.getFormattedPrefix() + s);
    }

    @Override
    public void info(Marker marker, String s, Object o) {
        delegate().info(marker, this.getFormattedPrefix() + s, o);
    }

    @Override
    public void info(Marker marker, String s, Object o, Object o1) {
        delegate().info(marker, this.getFormattedPrefix() + s, o, o1);
    }

    @Override
    public void info(Marker marker, String s, Object... objects) {
        delegate().info(marker, this.getFormattedPrefix() + s, objects);
    }

    @Override
    public void info(Marker marker, String s, Throwable throwable) {
        delegate().info(marker, this.getFormattedPrefix() + s, throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate().isWarnEnabled();
    }

    @Override
    public void warn(String s) {
        delegate().warn(this.getFormattedPrefix() + s);
    }

    @Override
    public void warn(String s, Object o) {
        delegate().warn(this.getFormattedPrefix() + s, o);
    }

    @Override
    public void warn(String s, Object... objects) {
        delegate().warn(this.getFormattedPrefix() + s, objects);
    }

    @Override
    public void warn(String s, Object o, Object o1) {
        delegate().warn(this.getFormattedPrefix() + s, o, o1);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        delegate().warn(this.getFormattedPrefix() + s, throwable);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return delegate().isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String s) {
        delegate().warn(marker, this.getFormattedPrefix() + s);
    }

    @Override
    public void warn(Marker marker, String s, Object o) {
        delegate().warn(marker, this.getFormattedPrefix() + s, o);
    }

    @Override
    public void warn(Marker marker, String s, Object o, Object o1) {
        delegate().warn(marker, this.getFormattedPrefix() + s, o, o1);
    }

    @Override
    public void warn(Marker marker, String s, Object... objects) {
        delegate().warn(marker, this.getFormattedPrefix() + s, objects);
    }

    @Override
    public void warn(Marker marker, String s, Throwable throwable) {
        delegate().warn(marker, this.getFormattedPrefix() + s, throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate().isErrorEnabled();
    }

    @Override
    public void error(String s) {
        delegate().error(this.getFormattedPrefix() + s);
    }

    @Override
    public void error(String s, Object o) {
        delegate().error(this.getFormattedPrefix() + s, o);
    }

    @Override
    public void error(String s, Object o, Object o1) {
        delegate().error(this.getFormattedPrefix() + s, o, o1);
    }

    @Override
    public void error(String s, Object... objects) {
        delegate().error(this.getFormattedPrefix() + s, objects);
    }

    @Override
    public void error(String s, Throwable throwable) {
        delegate().error(this.getFormattedPrefix() + s, throwable);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return delegate().isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String s) {
        delegate().error(marker, this.getFormattedPrefix() + s);
    }

    @Override
    public void error(Marker marker, String s, Object o) {
        delegate().error(marker, this.getFormattedPrefix() + s, o);
    }

    @Override
    public void error(Marker marker, String s, Object o, Object o1) {
        delegate().error(marker, this.getFormattedPrefix() + s, o, o1);
    }

    @Override
    public void error(Marker marker, String s, Object... objects) {
        delegate().error(marker, this.getFormattedPrefix() + s, objects);
    }

    @Override
    public void error(Marker marker, String s, Throwable throwable) {
        delegate().error(marker, this.getFormattedPrefix() + s, throwable);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        PrefixedLogger that = (PrefixedLogger) o;

        return this.getFormattedPrefix().equals(that.prefix) && this.delegate().equals(that.logger);
    }
    
    @Override
    public int hashCode() {
        return delegate().hashCode();
    }
    
    protected Logger delegate() {
        return this.logger;
    }

    protected void setDelegate(Logger logger) {
        this.logger = logger;
    }

    /** Return the formatted prefix that actually gets prepended to the log messages.
     *
     * This is the prefix String given at construction time, with the placeholder replaced by
     * prefixObject's String representation.
     *
     * @return formatted prefix prepended to log messages
     */
    public String getFormattedPrefix() {
        return (isPrintingSimulationDateEnabled() ? String.format("[%.3f] ", CloudSim.clock()) : "")
                + prefix;
    }

    /** Set the prefix of this logger, without changing the prefixObject.
     *
     * Check {@link #PrefixedLogger(Logger, String, Object)} for details on the prefix value and usage.
     *
     * @param prefix new prefix of the logger
     */
    public void setPrefix(String prefix) {
        this.setPrefix(prefix, this.getPrefixObject());
    }

    /** Set the prefix of this logger.
     *
     * Check {@link #PrefixedLogger(Logger, String, Object)} for details on the prefix value and usage.
     *
     * @param prefix new prefix of the logger
     * @param prefixObject new prefixObject associated with the logger
     */
    public void setPrefix(String prefix, Object prefixObject) {
        setPrefixObject(prefixObject);
        FormattingTuple ft = MessageFormatter.format(prefix, prefixObject);
        this.prefix = ft.getMessage();
    }

    public Object getPrefixObject() {
        return this.prefixObject;
    }

    /** Set the prefix Object of this logger.
     *
     * @param prefixObject new prefixObject associated with the logger
     */
    public void setPrefixObject(Object prefixObject) {
        this.prefixObject = prefixObject;
    }

    public void enablePrintingSimulationDate(boolean enable) {
        printSimulationDate = enable;
    }

    public boolean isPrintingSimulationDateEnabled() {
        return printSimulationDate;
    }
}
