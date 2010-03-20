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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Runs the WAR project locally on the Google App Engine development server. Add @execute
 * phase="package" if you want to package project before execute the goal You
 * can specify jvm flags via the jvmFlags in the configuration section.
 * 
 * @author rhansen@kindleit.net
 * @goal stop
 * @requiresDependencyResolution runtime
 */
public class StopGoal extends AbstractMojo {

	/**
	 * Number of seconds to delay.
	 * 
	 * @parameter expression="${seconds}" default-value="1"
	 */
	protected int seconds;

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info(
				String.format("Let's wait for another %d seconds", seconds));
		for (int i = 0; i < seconds; i++) {
			getLog().info("Sleep #" + (i + 1));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
