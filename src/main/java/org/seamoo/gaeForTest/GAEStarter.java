package org.seamoo.gaeForTest;

import com.google.appengine.tools.admin.OutputPump;
import com.google.appengine.tools.development.DevAppServerMain;
import com.google.appengine.tools.info.SdkInfo;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GAEStarter {

	public static void main(String args[]) {
		new GAEStarter(args);
	}

	public GAEStarter(String args[]) {
		serverProcess = null;
		String entryClass = null;
		ProcessBuilder builder = new ProcessBuilder(new String[0]);
		String home = System.getProperty("java.home");
		String javaExe = (new StringBuilder()).append(home).append(
				File.separator).append("bin").append(File.separator).append(
				"java").toString();
		List jvmArgs = new ArrayList();
		ArrayList appServerArgs = new ArrayList();
		List command = builder.command();
		command.add(javaExe);
		boolean startOnFirstThread = System.getProperty("os.name")
				.equalsIgnoreCase("Mac OS X");
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("--jvm_flag")) {
				int indexOfSplit = args[i].indexOf('=');
				if (indexOfSplit == -1) {
					String msg = "--jvm_flag syntax is --jvm_flag=<flag>\n--jvm_flag may be repeated to supply multiple flags";
					throw new IllegalArgumentException(msg);
				}
				String jvmArg = args[i].substring(indexOfSplit + 1);
				jvmArgs.add(jvmArg);
				continue;
			}
			if (args[i].startsWith("--startOnFirstThread=")) {
				String arg = args[i].substring(args[i].indexOf('=') + 1);
				startOnFirstThread = Boolean.valueOf(arg).booleanValue();
				continue;
			}
			if (entryClass == null) {
				if (args[i].charAt(0) == '-')
					throw new IllegalArgumentException(
							(new StringBuilder())
									.append(
											"Only --jvm_flag may precede classname, not ")
									.append(args[i]).toString());
				entryClass = args[i];
				if (!entryClass
						.equals(com.google.appengine.tools.development.DevAppServerMain.class
								.getName()))
					throw new IllegalArgumentException(
							"KickStart only works for DevAppServerMain");
			} else {
				appServerArgs.add(args[i]);
			}
		}

		if (entryClass == null)
			throw new IllegalArgumentException("missing entry classname");
		File newWorkingDir = newWorkingDir(args);
		builder.directory(newWorkingDir);
		if (startOnFirstThread)
			jvmArgs.add("-XstartOnFirstThread");
		String classpath = System.getProperty("java.class.path");
		StringBuffer newClassPath = new StringBuffer();
		if (classpath == null)
			throw new AssertionError("classpath must not be null");
		String paths[] = classpath.split(File.pathSeparator);
		for (int i = 0; i < paths.length; i++) {
			newClassPath.append((new File(paths[i])).getAbsolutePath());
			if (i != paths.length - 1)
				newClassPath.append(File.pathSeparator);
		}

		String sdkRoot = null;
		List absoluteAppServerArgs = new ArrayList(appServerArgs.size());
		for (int i = 0; i < appServerArgs.size(); i++) {
			String arg = (String) appServerArgs.get(i);
			if (arg.startsWith("--sdk_root=")) {
				sdkRoot = (new File(arg.split("=")[1])).getAbsolutePath();
				arg = (new StringBuilder()).append("--sdk_root=").append(
						sdkRoot).toString();
			} else if (i == appServerArgs.size() - 1)
				arg = (new File(arg)).getAbsolutePath();
			absoluteAppServerArgs.add(arg);
		}

		if (sdkRoot == null)
			sdkRoot = SdkInfo.getSdkRoot().getAbsolutePath();
		String agentJar = (new StringBuilder()).append(sdkRoot).append(
				"/lib/agent/appengine-agent.jar").toString();
		agentJar = agentJar.replace('/', File.separatorChar);
		jvmArgs.add((new StringBuilder()).append("-javaagent:")
				.append(agentJar).toString());
		command.addAll(jvmArgs);
		command.add("-classpath");
		command.add(newClassPath.toString());
		command.add(entryClass);
		command.addAll(absoluteAppServerArgs);
		logger.info((new StringBuilder()).append("Executing ").append(command)
				.toString());

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (serverProcess != null)
					serverProcess.destroy();
			}
		});
		try {
			serverProcess = builder.start();
		} catch (IOException e) {
			throw new RuntimeException("Unable to start the process", e);
		}

		(new Thread(new OutputPump(serverProcess.getErrorStream(),
				new PrintWriter(System.err, true)))).start();
	}

	public void waitForServerToBeTerminated() {
		try {
			serverProcess.waitFor();
		} catch (InterruptedException e) {
		}
		serverProcess.destroy();
		serverProcess = null;
	}

	private static File newWorkingDir(String args[]) {
		if (args.length < 1) {
			DevAppServerMain.printHelp(System.out);
			System.exit(1);
		}
		File newDir = new File(args[args.length - 1]);
		DevAppServerMain.validateWarPath(newDir);
		return newDir;
	}

	private static final Logger logger = Logger.getLogger(GAEStarter.class
			.getName());

	public Process getServerProcess() {
		return serverProcess;
	}

	private Process serverProcess;

}
