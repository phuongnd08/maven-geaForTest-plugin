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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

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

		(new Thread(new OutputPump(serverInputStream, new PrintWriter(
				System.out, true)))).start();

		getLog().info("Waiting for server to be ready");
		try {
			waitForWebServer(port);
			getLog().info("Server is ready");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			getLog().info("Error while waiting for server");
		}

	}

	private void waitForWebServer(int port) throws IOException {
		boolean portTaken = false;
		while (!portTaken) {
			ServerSocket socket = null;
			try {
				socket = new ServerSocket(port);
			} catch (IOException e) {
				portTaken = true;
			} finally {
				if (socket != null)
					socket.close();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
			}
		}

	}
}
