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

import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.Key;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.apigee.buildTools.enterprise4g.rest.RestUtil;



/**                                                                                                                                     ¡¡
 * Goal to deploy to Apigee archive
 * @author ssvaidyanathan
 * @goal deploy-archive
 * @phase install
 * 
 */

public class DeployArchiveMojo extends GatewayAbstractMojo
{
	
	public static class DeployArchive {
        @Key
        public String gcs_uri;
        @Key
        public Map<String, String> labels;
    }
	
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public static final String DEPLOYMENT_FAILED_MESSAGE = "\n\n\n* * * * * * * * * * *\n\n"
			+ "This deployment could have failed for a variety of reasons.\n\n"
			+ "\n\n* * * * * * * * * * *\n\n\n";

	static Logger logger = LoggerFactory.getLogger(DeployMojo.class);
	
	public DeployArchiveMojo() {
		super();

	}
	
	/** 
	 * Entry point for the mojo.
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {


		if (super.isSkip()) {
			getLog().info("Skipping");
			return;
		}

		try {
			
			logger.info("\n\n=============Generating Upload URI================\n\n");
			String uploadURL = RestUtil.generateUploadUrl(super.getProfile());
			
			logger.info("\n\n=============Uploading Archive================\n\n");
			RestUtil.uploadArchive(super.getProfile(), super.getApplicationBundlePath(), uploadURL);
			logger.info("\n\n=============Uploading Archive Complete================\n\n");
			
			logger.info("\n\n=============Deploying Archive================\n\n");
			DeployArchive archive = new DeployArchive();
			archive.gcs_uri = uploadURL;
			if(super.getProfile().getApigeeArchiveLabels()!=null)
				archive.labels = super.getProfile().getApigeeArchiveLabels();
			String name = RestUtil.deployArchive(super.getProfile(), gson.toJson(archive).toString());
			logger.info("\n\n=============Archive deployment Complete================\n\n");
			
			boolean deployed = false;
			//Get the operation Status - loop until FINISHED
			for (; !deployed; ) {
				deployed = RestUtil.getArchiveDeploymentStatus(super.getProfile(), name);
	        	Thread.sleep(5*1000);
			}
			
		} catch (RuntimeException e) {
			processHelpfulErrorMessage(e);
		} catch (Exception e) {
			processHelpfulErrorMessage(e);
		} finally {
			
		}
	}

	private void processHelpfulErrorMessage(Exception e)
			throws MojoExecutionException {
		if (e instanceof MojoExecutionException) {
			throw (MojoExecutionException) e;
		} else {
			throw new MojoExecutionException("", e);
		}

	}
}




