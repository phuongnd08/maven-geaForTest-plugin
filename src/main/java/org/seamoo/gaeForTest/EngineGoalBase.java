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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

/**
 * Base MOJO class for working with the Google App Engine SDK.
 * 
 * @author rhansen@kindleit.net
 */
public abstract class EngineGoalBase extends AbstractMojo {

	private static final String GAE_PROPS = "gae.properties";

	protected static final String[] ARG_TYPE = new String[0];

	/**
	 * The Maven project reference.
	 * 
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * The Maven settings reference.
	 * 
	 * @parameter expression="${settings}"
	 * @required
	 * @readonly
	 */
	protected Settings settings;

	/**
	 * Overrides where the Project War Directory is located.
	 * 
	 * @parameter 
	 *            expression="${project.build.directory}/${project.build.finalName}"
	 * @required
	 */
	protected String appDir;

	/**
	 * Specifies where the Google App Engine SDK is located.
	 * 
	 * @parameter expression="${gae.home}" default-value="${settings.localRepository}/com/google/appengine/appengine-java-sdk/${gae.version}/appengine-java-sdk-${gae.version}"
	 * @required
	 */
	protected String sdkDir;

	/**
	 * Split large jar files (> 10M) into smaller fragments.
	 * 
	 * @parameter expression="${gae.deps.split}" default-value="false"
	 */
	protected boolean splitJars;

	/**
	 * The username to use. Will prompt if omitted.
	 * 
	 * @parameter expression="${gae.email}"
	 */
	protected String emailAccount;

	/**
	 * The server to connect to.
	 * 
	 * @parameter expression="${gae.server}"
	 */
	protected String uploadServer;

	/**
	 * Overrides the Host header sent with all RPCs.
	 * 
	 * @parameter expression="${gae.host}"
	 */
	protected String hostString;

	/**
	 * Do not delete temporary directory used in uploading.
	 * 
	 * @parameter expression="${gae.keepTemps}" default-value="false"
	 */
	protected boolean keepTempUploadDir;

	/**
	 * Always read the login password from stdin.
	 * 
	 * @parameter expression="${gae.passin}" default-value="false"
	 */
	protected boolean passIn;

	/**
	 * Tell AppCfg to use a proxy.
	 * 
	 * @parameter expression="${gae.proxy}"
	 */
	protected String proxy;

	protected Properties gaeProperties;

	public EngineGoalBase() {
		gaeProperties = new Properties();
		try {
			gaeProperties.load(EngineGoalBase.class
					.getResourceAsStream(GAE_PROPS));
		} catch (final IOException e) {
			throw new RuntimeException("Unable to load version", e);
		}
	}

	protected final InputStream runGAEStarterAsync(final String startClass,
			final String... commandArguments) throws MojoExecutionException {

		final List<String> args = new ArrayList<String>();
		args.add(startClass);
		args.addAll(getCommonArgs());
		args.addAll(Arrays.asList(commandArguments));

		assureSystemProperties();

		final GAEStarter starter = new GAEStarter(args.toArray(ARG_TYPE));

		Thread GAEStarterThread = new Thread(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				starter.waitForServerToBeTerminated();
			}
		});
		GAEStarterThread.start();
		return starter.getServerProcess().getInputStream();
	}

	/**
	 * Groups alterations to System properties for the proper execution of the
	 * actual GAE code.
	 * 
	 * @throws MojoExecutionException
	 *             When the gae.home variable cannot be set.
	 */
	protected void assureSystemProperties() throws MojoExecutionException {
		// explicitly specify SDK root, as auto-discovery fails when
		// appengine-tools-api.jar is loaded from Maven repo, not SDK
		String sdk = System.getProperty("appengine.sdk.root");
		if (sdk == null) {
			if (sdkDir == null) {
				throw new MojoExecutionException(this,
						"${gae.home} property not set", gaeProperties
								.getProperty("home_undefined"));
			}
			System.setProperty("appengine.sdk.root", sdk = sdkDir);
		}

		if (!new File(sdk).isDirectory()) {
			throw new MojoExecutionException(this,
					"${gae.home} is not a directory", gaeProperties
							.getProperty("home_invalid"));
		}

		// hack for getting appengine-tools-api.jar on a runtime classpath
		// (GAEStarter checks java.class.path system property for classpath
		// entries)
		final String classpath = System.getProperty("java.class.path");
		final String toolsJar = sdkDir + "/lib/appengine-tools-api.jar";
		if (!classpath.contains(toolsJar)) {
			System.setProperty("java.class.path", classpath
					+ File.pathSeparator + toolsJar);
		}
	}

	/**
	 * Generate all common Google AppEngine Task Parameters for use in all the
	 * goals.
	 * 
	 * @return List of arguments to add.
	 */
	protected final List<String> getAppCfgArgs() {
		final List<String> args = getCommonArgs();

		addBooleanOption(args, "--disable_prompt", !settings
				.getInteractiveMode());

		addStringOption(args, "--email=", emailAccount);
		addStringOption(args, "--host=", hostString);
		addStringOption(args, "--proxy=", proxy);
		addBooleanOption(args, "--passin", passIn);
		addBooleanOption(args, "--enable_jar_splitting", splitJars);
		addBooleanOption(args, "--retain_upload_dir", keepTempUploadDir);

		return args;
	}

	protected final List<String> getCommonArgs() {
		final List<String> args = new ArrayList<String>(8);

		args.add("--sdk_root=" + sdkDir);
		addStringOption(args, "--server=", uploadServer);

		return args;
	}

	private final void addBooleanOption(final List<String> args,
			final String key, final boolean var) {
		if (var) {
			args.add(key);
		}
	}

	private final void addStringOption(final List<String> args,
			final String key, final String var) {
		if (var != null && var.length() > 0) {
			args.add(key + var);
		}
	}

}
