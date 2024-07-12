package me.bramar.extracommands.customenchants.java;

import java.net.URL;
import java.net.URLClassLoader;

public class JEClassLoader extends URLClassLoader {
    public JEClassLoader(URL url, ClassLoader parent) {
        super(new URL[] {url}, parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        }catch(ClassNotFoundException e1) {
            return Class.forName(name);
        }
    }
}
