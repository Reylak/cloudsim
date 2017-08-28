package org.cloudbus.cloudsim.util;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

public class EntityPrefixedLogger implements Logger {
    private Logger logger;
    private Object entity;
    private String prefix;

    /** Create a new Logger that wraps an existing logger by prepending a prefix built from an entity to log messages.
     *
     * prefix is expected to include exactly one SLF4J anchor placeholder `{}`, which is replaced by the String
     * representation of entity by the SLF4J mechanisms included in delegate().
     *
     * @param logger wrapped logger
     * @param prefix prefix to be prepended to all logged messages
     * @param entity entity whose String representation is included in the prepended prefix
     */
    public EntityPrefixedLogger(Logger logger, String prefix, Object entity) {
        this.setDelegate(logger);
        this.entity = entity;
        this.setPrefix(prefix, this.getEntity());
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
        delegate().trace(this.getFinalPrefix() + s);
    }

    @Override
    public void trace(String s, Object o) {
        delegate().trace(this.getFinalPrefix() + s, o);
    }

    @Override
    public void trace(String s, Object o, Object o1) {
        delegate().trace(this.getFinalPrefix() + s, o, o1);
    }

    @Override
    public void trace(String s, Object... objects) {
        delegate().trace(this.getFinalPrefix() + s, objects);
    }

    @Override
    public void trace(String s, Throwable throwable) {
        delegate().trace(this.getFinalPrefix() + s, throwable);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return delegate().isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String s) {
        delegate().trace(marker, this.getFinalPrefix() + s);
    }

    @Override
    public void trace(Marker marker, String s, Object o) {
        delegate().trace(marker, this.getFinalPrefix() + s, o);
    }

    @Override
    public void trace(Marker marker, String s, Object o, Object o1) {
        delegate().trace(marker, this.getFinalPrefix() + s, o, o1);
    }

    @Override
    public void trace(Marker marker, String s, Object... objects) {
        delegate().trace(marker, this.getFinalPrefix() + s, objects);
    }
    
    @Override
    public void trace(Marker marker, String s, Throwable throwable) {
        delegate().trace(marker, this.getFinalPrefix() + s, throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate().isDebugEnabled();
    }

    @Override
    public void debug(String s) {
        delegate().debug(this.getFinalPrefix() + s);
    }

    @Override
    public void debug(String s, Object o) {
        delegate().debug(this.getFinalPrefix() + s, o);
    }

    @Override
    public void debug(String s, Object o, Object o1) {
        delegate().debug(this.getFinalPrefix() + s, o, o1);
    }

    @Override
    public void debug(String s, Object... objects) {
        delegate().debug(this.getFinalPrefix() + s, objects);
    }

    @Override
    public void debug(String s, Throwable throwable) {
        delegate().debug(this.getFinalPrefix() + s, throwable);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return delegate().isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String s) {
        delegate().debug(marker, this.getFinalPrefix() + s);
    }

    @Override
    public void debug(Marker marker, String s, Object o) {
        delegate().debug(marker, this.getFinalPrefix() + s, o);
    }

    @Override
    public void debug(Marker marker, String s, Object o, Object o1) {
        delegate().debug(marker, this.getFinalPrefix() + s, o, o1);
    }

    @Override
    public void debug(Marker marker, String s, Object... objects) {
        delegate().debug(marker, this.getFinalPrefix() + s, objects);
    }

    @Override
    public void debug(Marker marker, String s, Throwable throwable) {
        delegate().debug(marker, this.getFinalPrefix() + s, throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate().isInfoEnabled();
    }

    @Override
    public void info(String s) {
        delegate().info(this.getFinalPrefix() + s);
    }

    @Override
    public void info(String s, Object o) {
        delegate().info(this.getFinalPrefix() + s, o);
    }

    @Override
    public void info(String s, Object o, Object o1) {
        delegate().info(this.getFinalPrefix() + s, o, o1);
    }

    @Override
    public void info(String s, Object... objects) {
        delegate().info(this.getFinalPrefix() + s, objects);
    }

    @Override
    public void info(String s, Throwable throwable) {
        delegate().info(this.getFinalPrefix() + s, throwable);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return delegate().isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String s) {
        delegate().info(marker, this.getFinalPrefix() + s);
    }

    @Override
    public void info(Marker marker, String s, Object o) {
        delegate().info(marker, this.getFinalPrefix() + s, o);
    }

    @Override
    public void info(Marker marker, String s, Object o, Object o1) {
        delegate().info(marker, this.getFinalPrefix() + s, o, o1);
    }

    @Override
    public void info(Marker marker, String s, Object... objects) {
        delegate().info(marker, this.getFinalPrefix() + s, objects);
    }

    @Override
    public void info(Marker marker, String s, Throwable throwable) {
        delegate().info(marker, this.getFinalPrefix() + s, throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate().isWarnEnabled();
    }

    @Override
    public void warn(String s) {
        delegate().warn(this.getFinalPrefix() + s);
    }

    @Override
    public void warn(String s, Object o) {
        delegate().warn(this.getFinalPrefix() + s, o);
    }

    @Override
    public void warn(String s, Object... objects) {
        delegate().warn(this.getFinalPrefix() + s, objects);
    }

    @Override
    public void warn(String s, Object o, Object o1) {
        delegate().warn(this.getFinalPrefix() + s, o, o1);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        delegate().warn(this.getFinalPrefix() + s, throwable);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return delegate().isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String s) {
        delegate().warn(marker, this.getFinalPrefix() + s);
    }

    @Override
    public void warn(Marker marker, String s, Object o) {
        delegate().warn(marker, this.getFinalPrefix() + s, o);
    }

    @Override
    public void warn(Marker marker, String s, Object o, Object o1) {
        delegate().warn(marker, this.getFinalPrefix() + s, o, o1);
    }

    @Override
    public void warn(Marker marker, String s, Object... objects) {
        delegate().warn(marker, this.getFinalPrefix() + s, objects);
    }

    @Override
    public void warn(Marker marker, String s, Throwable throwable) {
        delegate().warn(marker, this.getFinalPrefix() + s, throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate().isErrorEnabled();
    }

    @Override
    public void error(String s) {
        delegate().error(this.getFinalPrefix() + s);
    }

    @Override
    public void error(String s, Object o) {
        delegate().error(this.getFinalPrefix() + s, o);
    }

    @Override
    public void error(String s, Object o, Object o1) {
        delegate().error(this.getFinalPrefix() + s, o, o1);
    }

    @Override
    public void error(String s, Object... objects) {
        delegate().error(this.getFinalPrefix() + s, objects);
    }

    @Override
    public void error(String s, Throwable throwable) {
        delegate().error(this.getFinalPrefix() + s, throwable);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return delegate().isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String s) {
        delegate().error(marker, this.getFinalPrefix() + s);
    }

    @Override
    public void error(Marker marker, String s, Object o) {
        delegate().error(marker, this.getFinalPrefix() + s, o);
    }

    @Override
    public void error(Marker marker, String s, Object o, Object o1) {
        delegate().error(marker, this.getFinalPrefix() + s, o, o1);
    }

    @Override
    public void error(Marker marker, String s, Object... objects) {
        delegate().error(marker, this.getFinalPrefix() + s, objects);
    }

    @Override
    public void error(Marker marker, String s, Throwable throwable) {
        delegate().error(marker, this.getFinalPrefix() + s, throwable);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        EntityPrefixedLogger that = (EntityPrefixedLogger) o;

        return this.getFinalPrefix().equals(that.prefix) && this.delegate().equals(that.logger);
    }
    
    @Override
    public int hashCode() {
        return delegate().hashCode();
    }
    
    public Logger delegate() {
        return this.logger;
    }

    public void setDelegate(Logger logger) {
        this.logger = logger;
    }

    /** Return the final prefix that actually get prepended to the log messages.
     *
     * This is the prefix String given at construction time, with the placeholder replaced by the entity's String
     * representation.
     *
     * @return final prefix prepended to log messages
     */
    public String getFinalPrefix() {
        return this.prefix;
    }

    /** Set the prefix of this logger, without changing the entity.
     *
     * Check {@link #EntityPrefixedLogger(Logger, String, Object)} for details on the prefix value and usage.
     *
     * @param prefix new prefix of the logger
     */
    public void setPrefix(String prefix) {
        this.setPrefix(prefix, this.getEntity());
    }

    /** Set the prefix of this logger.
     *
     * Check {@link #EntityPrefixedLogger(Logger, String, Object)} for details on the prefix value and usage.
     *
     * @param prefix new prefix of the logger
     * @param entity new entity associated to the logger
     */
    public void setPrefix(String prefix, Object entity) {
        FormattingTuple ft = MessageFormatter.format(prefix, entity);
        this.prefix = ft.getMessage();
    }

    public Object getEntity() {
        return this.entity;
    }
}
