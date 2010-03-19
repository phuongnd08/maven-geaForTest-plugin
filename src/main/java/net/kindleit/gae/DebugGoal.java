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
package net.kindleit.gae;

import java.util.ArrayList;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Extends the {@link RunGoal} for running the project with a debugger port hook (optionally suspended).
 *
 * It is a simple utility method, as the run goal supports the passing of jvm options in the command line.
 *
 * @author rhansen@kindleit.net
 * @author androns@gmail.com
 * @goal debug
 * @requiresDependencyResolution runtime
 * @execute phase="package"
 */
public class DebugGoal extends RunGoal {

  /** Port to run in.
   *
   * @parameter expression="${gae.debugPort}" default-value="8000"
   */
  protected int debugPort;

  /** Address to bind to.
   *
   * @parameter expression="${gae.debugSuspend}" default-value="true"
   */
  protected boolean debugSuspend;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (jvmFlags == null) {
      jvmFlags = new ArrayList<String>(2);
    }

    jvmFlags.add("-Xdebug");
    jvmFlags.add("-Xrunjdwp:transport=dt_socket,server=y,suspend="
        + (debugSuspend ? "y" : "n") + ",address=" + debugPort);

    super.execute();
  }

}
