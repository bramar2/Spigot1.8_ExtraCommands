package me.bramar.extracommands.customenchants.java;

import me.bramar.extracommands.Main;
import me.bramar.extracommands.customenchants.CustomEnchantment;
import me.bramar.extracommands.customenchants.EnchantLoader;
import org.apache.commons.io.FileUtils;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class JavaEnchantLoader {
    private final Path PACKAGE_PREPEND = Paths.get("me/bramar/extracommands/javaenchants/");
    private final Main main;
    private final EnchantLoader enchantLoader;
    private final List<CustomEnchantment> enchants = new ArrayList<>();

    public JavaEnchantLoader(EnchantLoader enchantLoader) {
        this.main = Main.getInstance();
        this.enchantLoader = enchantLoader;
        main.getLogger().info("[CustomEnchants/JavaEnchants] Loading Java (.java) enchants...");
        loadEnchants();
    }
    private void searchClassPath(File folder, List<String> list) {
        if(!folder.exists())
            return;
        File[] files = folder.listFiles();
        if(files == null)
            return;
        for(File file : files) {
            if(file.isDirectory())
                searchClassPath(file, list);
            else {
                if(file.getName().endsWith(".jar")) {
                    list.add(file.getAbsolutePath());
                }
            }
        }
    }

    private void loadEnchants() {
        main.getLogger().info("[CustomEnchants/JavaEnchants] Searching files (deep-search)...");
        List<File> javaFiles = new ArrayList<>();
        File enchantsFolder = new File(main.getDataFolder(), "enchants");
        search(enchantsFolder, javaFiles);
        if(javaFiles.isEmpty()) {
            main.getLogger().info("[CustomEnchants/JavaEnchants] No .java files found within enchants folder, shutting down JavaEnchants.");
            return;
        }
        main.getLogger().info("[CustomEnchants/JavaEnchants] Found " + javaFiles.size() + " .java files");
        main.getLogger().info("[CustomEnchants/JavaEnchants] Searching JARs for the classpath (deep-search)...");
        List<String> jarFiles = new ArrayList<>();
        searchClassPath(new File(System.getProperty("user.dir")), jarFiles);
        String compilerClassPath = "";
        for(String jarPath : jarFiles) {
            compilerClassPath += jarPath + ";";
        }
        boolean classPathExists = !compilerClassPath.isEmpty();
        if(classPathExists)
            compilerClassPath = compilerClassPath.substring(0, compilerClassPath.length() - 1);
        main.getLogger().info("[CustomEnchants/JavaEnchants] Loading Java classes...");
        File javaEnchantDir = new File(main.getDataFolder(), "java-enchants");
        try {
            FileUtils.deleteDirectory(javaEnchantDir);
            if(!javaEnchantDir.exists() && !javaEnchantDir.mkdirs())
                throw new IOException("unknown exception: Failed to mkdirs()/create directory");
        }catch(IOException e) {
            e.printStackTrace();
            main.getLogger().warning("[CustomEnchants/JavaEnchants] Failed to reset java-enchants directory, not loading java enchants!");
            return;
        }
        Path rootPath = enchantsFolder.toPath();
        File src = new File(javaEnchantDir, "src");
        File bin = new File(javaEnchantDir, "bin");
        src.mkdirs(); bin.mkdirs();
        URL url = null;
        try {
            url = bin.toURI().toURL();
        }catch(MalformedURLException e) {
            e.printStackTrace();
            main.getLogger().warning("[CustomEnchants/JavaEnchants] Failed to get directory's URL for loading classes");
            return;
        }
        URLClassLoader classLoader = new JEClassLoader(url, ClassLoader.getSystemClassLoader());
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        for(File javaFile : javaFiles) {
            Path path = rootPath.relativize(javaFile.toPath());
            String className = javaFile.getName();
            className = className.substring(0, className.lastIndexOf('.'));
            Path classPath = PACKAGE_PREPEND.resolve(path);
            File srcFile = new File(src, classPath.toString());
            File binFile = new File(bin, classPath.toString().replaceAll("\\.java$", ".class"));
            if(!srcFile.getParentFile().exists() && !srcFile.getParentFile().mkdirs()) {
                main.getLogger().warning("[CustomEnchants/JavaEnchants] Failed to load " + path + ": Failed to create Parent Directory (SRC)");
                continue;
            }
            if(!binFile.getParentFile().exists() && !binFile.getParentFile().mkdirs()) {
                main.getLogger().warning("[CustomEnchants/JavaEnchants] Failed to load " + path + ": Failed to create Parent Directory (BIN)");
                continue;
            }
            try {
                Files.copy(javaFile.toPath(), srcFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }catch(IOException e) {
                e.printStackTrace();
                main.getLogger().warning("[CustomEnchants/JavaEnchants] Failed to load " + path + ": Failed to copy to src");
                continue;
            }
            // Modify 'package com.whatever' in the contents
            String packageName = classPath.getParent().toString().replace(File.separator, ".")
                    .replaceAll("\\.+$", "").replaceAll("^\\.+", "");
            String packageText = "package " + packageName + ";";
            StringBuilder contents = new StringBuilder();

            try {
                FileReader fr = new FileReader(srcFile);
                BufferedReader reader = new BufferedReader(fr);
                String line;
                boolean replaced = false;
                while((line = reader.readLine()) != null) {
                    if(!replaced) {
                        String lineReplaced = line.replaceAll("[pP][aA][cC][kK][aA][gG][eE][ ]+.*;", packageText);
                        if(lineReplaced.toLowerCase().contains("package")) {
                            replaced = true;
                        }
                        contents.append(lineReplaced).append('\n');
                    }else
                        contents.append(line).append('\n');
                }
                reader.close();
                fr.close();
                if(!replaced) {
                    contents.insert(0, '\n');
                    contents.insert(0, packageText);
                }
                FileWriter fw = new FileWriter(srcFile);
                BufferedWriter writer = new BufferedWriter(fw);
                String contentsStr = contents.toString();
                writer.write(contentsStr, 0, contentsStr.length());
                writer.close();
                fw.close();
            }catch(IOException e) {
                e.printStackTrace();
                main.getLogger().warning("[CustomEnchants/JavaEnchants] Failed to load " + path + ": Failed to modify package name");
                continue;
            }
            // Compile
            int code = compiler.run(null, null, null, "-classpath", compilerClassPath, "-d", bin.getAbsolutePath(), srcFile.getAbsolutePath());
            if(code != 0) {
                main.getLogger().info("[CustomEnchants/JavaEnchants] Failed to load " + path + ": Failed to compile");
                continue;
            }
            main.getLogger().info("[CustomEnchants/JavaEnchants] Compiled file " + path);
            Class<?> clazz;
            try {
                String cls = packageName + "." + className;
                System.out.println("CLASS: " + cls);
                clazz = classLoader.loadClass(cls);

            }catch(ClassNotFoundException|NoClassDefFoundError e) {
                e.printStackTrace();
                main.getLogger().warning("[CustomEnchants/JavaEnchants] Failed to load " + path + ": Error at loading .class");
                continue;
            }
            main.getLogger().info("[CustomEnchants/JavaEnchants] Loaded file " + path);
            if(CustomEnchantment.class.isAssignableFrom(clazz)) {
                Class<? extends CustomEnchantment> clazz2 = (Class<? extends CustomEnchantment>) clazz;
                try {
                    CustomEnchantment customEnchantment = clazz2.newInstance();
                    enchants.add(customEnchantment);
                    main.getLogger().info("[CustomEnchants/JavaEnchants] Loaded JavaEnchant " + customEnchantment.getName());
                }catch(InstantiationException|IllegalAccessException e) {
                    e.printStackTrace();
                    main.getLogger().warning("[CustomEnchants/JavaEnchants] Failed to load file " + path + ": cannot create new CustomEnchantment instance");
                }
            }else {
                main.getLogger().warning("[CustomEnchants/JavaEnchants] Ignoring file " + path + " (does not extend CustomEnchantment)");
            }
        }
        main.getLogger().warning("[CustomEnchants/JavaEnchants] Loaded " + enchants.size() + " JavaEnchants");
        enchantLoader.getEnchants().addAll(enchants);
    }
    private void search(File folder, List<File> list) {
        if(!folder.exists() || !folder.isDirectory())
            return;
        File[] files = folder.listFiles();
        if(files == null)
            return;
        for(File file : files) {
            if(file.isDirectory()) {
                search(file, list);
                continue;
            }
            if(file.getName().endsWith(".java")) {
                if(file.getName().startsWith("-"))
                    continue; // excluded
                list.add(file);
            }
        }
    }
}
