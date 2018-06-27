package com.java.nio.examples;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class NIO_Path_Files {
	public static void main(String[] args) throws IOException {
		String currentDir = "C:\\2-Personal\\github\\java-nio-server-master\\java-nio-server-master";
		Path path = Paths.get(currentDir, "test.symbolic");
		System.out.println(path.normalize().toAbsolutePath().toString());
		System.out.println(Files.exists(path, LinkOption.NOFOLLOW_LINKS));
		
		System.out.println("before creating:" +Files.exists(path, LinkOption.NOFOLLOW_LINKS));
		path = Paths.get(currentDir, "createDir");
		Files.createDirectory(path);
		System.out.println("created:" + Files.exists(path, LinkOption.NOFOLLOW_LINKS));

		Files.delete(path);
		System.out.println("deleted:" + Files.exists(path, LinkOption.NOFOLLOW_LINKS));
		
		// walkFileTree #1: searching a file
		Path pathTree = Paths.get(currentDir, "file_tree");
		String fileToFind = java.io.File.separator + "5.txt";
		try {
			Files.walkFileTree(pathTree, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					String fileString = file.toAbsolutePath().toString();
					System.out.println(fileString);
					
					if (fileString.endsWith(fileToFind)) {
						System.out.println("file is found at: " + fileString);
						return FileVisitResult.TERMINATE;
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// walkFileTree #2: deleting the entire directory
		try {
			Files.walkFileTree(pathTree, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					System.out.println("deleting " + file.toString());
					return FileVisitResult.CONTINUE;
				}
				 @Override
				public FileVisitResult postVisitDirectory(Path dir, IOException ex) throws IOException {
					Files.delete(dir);
					System.out.println("deleting " + dir.toString());
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
