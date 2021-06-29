package blazingtwist.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

public class LogbackLoggerProvider {
	private static LoggerContext _context = null;
	private static PatternLayoutEncoder _fileEncoder = null;
	private static PatternLayoutEncoder _consoleEncoder = null;
	private static FileAppender<ILoggingEvent> _fileAppender = null;
	private static ConsoleAppender<ILoggingEvent> _consoleAppender = null;

	private static LoggerContext getContext() {
		if (_context == null) {
			_context = (LoggerContext) LoggerFactory.getILoggerFactory();
		}
		return _context;
	}

	private static PatternLayoutEncoder getFileEncoder() {
		if (_fileEncoder == null) {
			_fileEncoder = new PatternLayoutEncoder();
			_fileEncoder.setPattern("%date [%thread] %-5level : %logger - %msg%n");
			_fileEncoder.setContext(getContext());
			_fileEncoder.start();
		}
		return _fileEncoder;
	}

	private static PatternLayoutEncoder getConsoleEncoder() {
		if (_consoleEncoder == null) {
			PatternLayout.defaultConverterMap.put("highlightConsole", LogbackHighlightingCompositeConverter.class.getName());

			_consoleEncoder = new PatternLayoutEncoder();
			_consoleEncoder.setPattern("%white(%date{HH:mm:ss.SSS})" +
					" %cyan([%thread])" +
					" %highlightConsole(%-5level) :" +
					" %magenta(%logger)" +
					" - %msg" +
					"%n%nopex%highlightConsole(%ex)");
			_consoleEncoder.setContext(getContext());
			_consoleEncoder.start();
		}
		return _consoleEncoder;
	}

	private static FileAppender<ILoggingEvent> getFileAppender() {
		if (_fileAppender == null) {
			_fileAppender = new FileAppender<>();
			_fileAppender.setFile("target/testFile2.log");
			_fileAppender.setAppend(true);
			_fileAppender.setEncoder(getFileEncoder());
			_fileAppender.setContext(getContext());
			_fileAppender.start();
		}
		return _fileAppender;
	}

	private static ConsoleAppender<ILoggingEvent> getConsoleAppender() {
		if (_consoleAppender == null) {
			_consoleAppender = new ConsoleAppender<>();
			_consoleAppender.setEncoder(getConsoleEncoder());
			_consoleAppender.setTarget("System.out");
			_consoleAppender.setContext(getContext());
			_consoleAppender.start();
		}
		return _consoleAppender;
	}

	public static Logger getLogger(Class<?> owningClass) {
		Logger logger = (Logger) LoggerFactory.getLogger(owningClass.getCanonicalName());
		logger.addAppender(getFileAppender());
		logger.addAppender(getConsoleAppender());
		logger.setLevel(Level.TRACE);
		logger.setAdditive(false);

		return logger;
	}
}
