package com.apigee.hybrid.overrides;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import io.apigee.buildTools.enterprise4g.mavenplugin.DeployMojo;
import io.apigee.buildTools.enterprise4g.utils.FileReader;

public class OverridesAdaptor {
	
	static Logger logger = LoggerFactory.getLogger(DeployMojo.class);
	
	@SuppressWarnings("unchecked")
	public static void updateOverrides(String file, String env, String proxyDir) throws IOException, Exception {
		Yaml yaml = new Yaml();
		List<String> basepath = null;
		Map<String, Object> obj = yaml.load(new FileInputStream(file));
		List <Map<String, Object>> envs = (List<Map<String, Object>>) obj.get("envs");
		for (Map<String, Object> e : envs) {
			if(e.get("name").equals(env)) {
				basepath = getBasePath(proxyDir);
				Map<String, Object> paths = (Map<String, Object>) e.get("paths");
				if(paths != null) {
					Map<String, Object> uri = (Map<String, Object>) paths.get("uri");
					if(uri != null) {
						List<String> prefixList = (List<String>) uri.get("prefixes");
						if(prefixList==null || prefixList.size()==0) {
							prefixList = new ArrayList<String>();
							prefixList.addAll(basepath);
							uri.put("prefixes", prefixList);
						}
						else if(prefixList!=null && prefixList.size()>0 && prefixList.containsAll(basepath)) {
							logger.info("Skipping the overrides file update as the basepath already exists !!");
							return;
						}
						else if(prefixList!=null && prefixList.size()>0 && !prefixList.containsAll(basepath)){
							prefixList.addAll(basepath);
							uri.put("prefixes", prefixList);
						}
					}else {
						List<String> prefixList = new ArrayList<String>();
						prefixList.addAll(basepath);
						uri = new HashMap<String, Object>();
						uri.put("prefixes", prefixList);
						paths.put("uri", uri);
						e.put("paths", paths);
					}
				}else {
					List<String> prefixList = new ArrayList<String>();
					prefixList.addAll(basepath);
					Map<String, Object> uri = new HashMap<String, Object>();
					uri.put("prefixes", prefixList);
					paths = new HashMap<String, Object>();
					paths.put("uri", uri);
					e.put("paths", paths);
				}
				
				DumperOptions options = new DumperOptions();
				options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
				options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
				options.setPrettyFlow(true);
				yaml = new Yaml(options);
				String stringContent = yaml.dump(obj);
				Files.write(Paths.get(file), stringContent.getBytes());
				logger.info("Overrides file update with basepath is successful");
			}	
		}
	}
	
	private static List<String> getBasePath(String proxyDir) throws IOException, Exception{
		List<String> basepath = new ArrayList<String>();
		//Get the list of proxyendpoint files
		File folder = new File(proxyDir);
		File[] files = folder.listFiles();
		for (int i = 0; i < files.length; i++) {
			logger.info("Proxy endpoint: "+files[i].getName());
			FileReader fileReader = new FileReader();
			Document xmlDoc = fileReader.getXMLDocument(files[i]);
			javax.xml.xpath.XPathFactory factory = javax.xml.xpath.XPathFactory.newInstance();
	        javax.xml.xpath.XPath xpath = factory.newXPath();
	        javax.xml.xpath.XPathExpression expression = xpath.compile("/ProxyEndpoint/HTTPProxyConnection/BasePath");
	        Node node = (Node) expression.evaluate(xmlDoc, XPathConstants.NODE);
	        if(node!=null && node.hasChildNodes()) {
	        	logger.info("basepath: "+ expression.evaluate(xmlDoc));
	        	basepath.add(expression.evaluate(xmlDoc));
	        }
		}
		return basepath;
	}
}
