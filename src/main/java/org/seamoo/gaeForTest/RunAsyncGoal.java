/* Copyright 2009 Kindleit.net Software Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.seamoo.gaeForTest;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.google.appengine.tools.admin.OutputPump;

/**
 * Runs the WAR project locally on the Google App Engine development server. Add @execute
 * phase="package" if you want to package project before execute the goal You
 * can specify jvm flags via the jvmFlags in the configuration section.
 * 
 * @author rhansen@kindleit.net
 * @goal runAsync
 * @requiresDependencyResolution runtime
 */
public class RunAsyncGoal extends EngineGoalBase {

	/**
	 * Port to run in.
	 * 
	 * @parameter expression="${gae.testPort}" default-value="8081"
	 */
	protected int port;

	/**
	 * Address to bind to.
	 * 
	 * @parameter expression="${gae.address}" default-value="0.0.0.0"
	 */
	protected String address;

	/**
	 * Do not check for new SDK versions.
	 * 
	 * @parameter expression="${gae.disableUpdateCheck}" default-value="false"
	 */
	protected boolean disableUpdateCheck;

	/**
	 * Arbitrary list of JVM Flags to send to the KickStart task.
	 * 
	 * @parameter
	 */
	protected List<String> jvmFlags;

	private final String StartedSignal = "The server is running at http://localhost:%d/";

	public void execute() throws MojoExecutionException, MojoFailureException {
		final List<String> arguments = new ArrayList<String>();

		arguments.add("--address=" + address);
		arguments.add("--port=" + port);
		if (disableUpdateCheck) {
			arguments.add("--disable_update_check");
		}
		if (jvmFlags != null) {
			for (final String jvmFlag : jvmFlags) {
				arguments.add("--jvm_flag=" + jvmFlag);
			}
		}
		arguments.add(appDir);

		getLog().info("Start server");
		InputStream serverInputStream = runGAEStarterAsync(
				"com.google.appengine.tools.development.DevAppServerMain",
				arguments.toArray(new String[] {}));

		getLog().info("Waiting for server to be ready");
		try {
			waitForString(serverInputStream, String.format(StartedSignal, port));
			getLog().info("Server is ready");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			getLog().info("Error while waiting for server");
		}
		(new Thread(new OutputPump(serverInputStream, new PrintWriter(
				System.out, true)))).start();

	}

	private void waitForString(InputStream stream, String str)
			throws IOException {
		int ch = 0;
		StringBuilder strBuilder = new StringBuilder();
		while (ch != -1) {
			ch = stream.read();
			if (ch != -1)
				strBuilder.append((char) ch);// may be risky if the string
			// is on the last line and
			// the new line won't be
			// produced for quite long
			// time
			if (strBuilder.toString().contains(str)) {
				getLog().info(strBuilder.toString());
				return;
			}
		}

	}
}
