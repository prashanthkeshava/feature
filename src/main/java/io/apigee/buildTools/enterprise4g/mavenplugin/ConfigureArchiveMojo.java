/**
 * Copyright (C) 2021 Google 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apigee.buildTools.enterprise4g.mavenplugin;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.apigee.buildTools.enterprise4g.utils.ZipUtils;


/**
 * Goal to package an Apigee archive
 *
 * @author ssvaidyanathan
 * @goal configure-archive
 * @phase package
 */

public class ConfigureArchiveMojo extends GatewayAbstractMojo {


	public void execute() throws MojoExecutionException, MojoFailureException {

		if (super.isSkip()) {
			getLog().info("Skipping");
			return;
		}

		Logger logger = LoggerFactory.getLogger(ConfigureArchiveMojo.class);
		logger.info("\n\n=============Now zipping the Apigee archive================\n\n");
		//Zip package
		zipDirectory();
	}
	
	private void zipDirectory() throws MojoExecutionException{
		try {
			
			ZipUtils zu = new ZipUtils();
			zu.zipDir(new File(super.getApplicationBundlePath()),
					new File(super.getBuildDirectory() + "/src"), "src");
			
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage());
		}
	}
	
}
