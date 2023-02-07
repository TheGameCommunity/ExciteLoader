package com.thegamecommunity.ExciteLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.net.URLClassLoader;

public class Main {

	public static void main(String[] args) throws Throwable {
		
		try {
			System.setOut(new LogStream(System.out));
			System.setErr(new LogStream(System.err));
			System.out.println("Logging initialized.");
		} catch (FileNotFoundException e) {
			throw new AssertionError(e);
		}
		
		for(String arg : args) {
			if(arg.startsWith("gameJarPath=")) {
				System.setProperty("fabric.gameJarPath", arg.split("=")[1]);
			}
			else if (arg.startsWith("fabricLoaderPath=")) {
				System.setProperty("exciteloader.fabricPath", arg.split("=")[1]);
			}
			else if (arg.startsWith("fabricDependencyPath=")) {
				System.setProperty("exciteloader.fabricDependencyPath", arg.split("=")[1]);
			}
		}
		
		String gameJar = System.getProperty("fabric.gameJarPath");
		String fabricJar = System.getProperty("exciteloader.fabricPath");
		String fabricDependencyDir = System.getProperty("exciteloader.fabricDependencyPath");
		
		if(gameJar == null) {
			System.setProperty("fabric.gameJarPath", "./Excitebot.jar");
			gameJar = System.getProperty("fabric.gameJarPath");
		}
		if(fabricJar == null) {
			for(File file : new File(".").listFiles()) {
				if(file.getName().startsWith("fabric-loader-")) {
					System.setProperty("exciteloader.fabricPath", file.getCanonicalPath());
				}
			}
			fabricJar = System.getProperty("exciteloader.fabricPath");

		}
		if(fabricDependencyDir == null) {
			System.setProperty("exciteloader.fabricDependencyPath", "./fabric/");
			fabricDependencyDir = System.getProperty("exciteloader.fabricDependencyPath");
		}
		
		if(fabricJar == null) {
			throw new LinkageError("Missig Fabric Loader");
		}
		
		URL fabricJarPath = new File(fabricJar).toURI().toURL();
		File fabricDependencyPath = new File(fabricDependencyDir);
		File[] fabricDependencyFiles = fabricDependencyPath.listFiles();
		URL[] fabricDependencies = new URL[fabricDependencyFiles.length + 1];
		int i = 0;
		for(; i < fabricDependencyFiles.length; i++) {
			fabricDependencies[i] = fabricDependencyFiles[i].toURI().toURL();
			System.out.println("found fabric dependency " + fabricDependencies[i]);
		}
		fabricDependencies[i] = fabricJarPath;
		
		URLClassLoader launchClassLoader = new URLClassLoader(fabricDependencies);
		
		Thread.currentThread().setContextClassLoader(launchClassLoader); //have to set the context classloader, otherwise serviceLoader will not find the game provider
		
		MethodHandle handle = MethodHandles.publicLookup().findStatic(launchClassLoader.loadClass("net.fabricmc.loader.impl.launch.knot.KnotClient"), "main", MethodType.methodType(void.class, String[].class));
		handle.invokeExact(args);
		
		
	}
	
}
