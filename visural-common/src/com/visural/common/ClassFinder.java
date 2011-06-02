/*
 * Copyright 2005 Joe Walker, Jose Noheda, Richard Nichols
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.visural.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;

/**
 * ClassFinder enables you to find the set of classes that match certain
 * criteria.
 *
 * Based on org.directwebremoting.impl.ClasspathScanner
 * 
 * @author Jose Noheda [jose.noheda at gmail dot com]
 * @author Joe Walker [joe at getahead dot ltd dot uk]
 * @author Richard Nichols [tibes80 @ gmail dot com]
 */
public class ClassFinder {
    
    private final String packageName;
    private final boolean recursive;
    private final Collection<Class> superClassFilter = new ArrayList<Class>();
    private final Collection<Class> interfaceFilter = new ArrayList<Class>();
    private final Collection<Class> notSuperClassFilter = new ArrayList<Class>();
    private final Collection<String> classNameRegexFilter = new ArrayList<String>();
    private final Collection<Class> classAnnotationFilter = new ArrayList<Class>();
    private final Collection<Class> methodAnnotationFilter = new ArrayList<Class>();

    /**
     * Attempt to find all classes in the VM
     */
    public ClassFinder() {
        this(null, true);
    }

    /**
     * Non recursively find classes within the given package
     * @param packageName package name specified with dot separators
     */
    public ClassFinder(String packageName) {
        this(packageName, false);
    }

    /**
     * Find classes within the given package (optionally recursively)
     * @param packageName package name specified with dot separator
     * @param recursive True to dig into sub-packages
     */
    public ClassFinder(String packageName, boolean recursive) {
        this.recursive = recursive;

        if (packageName == null || packageName.equals("")) {
            packageName = "";
        }

        packageName = packageName.replace('.', '/');

        if (packageName.endsWith("*")) {
            packageName = packageName.substring(0, packageName.length() - 1);
        }

        if (packageName.endsWith("/")) {
            packageName = packageName.substring(0, packageName.length() - 1);
        }

        this.packageName = packageName;
    }

    public Set<Class> find() throws ClassNotFoundException {
        Set<Class> result = new HashSet<Class>();
        Set<String> classes = getClasses();
        for (String clazz : classes) {
            Class matched = filterClass(clazz);
            if(matched != null){
                result.add(matched);
            }
        }
        return result;
    }

    private Class filterClass(String className) throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Class clazz = classLoader.loadClass(className);
            if (filterNames(className) 
                    && filterSuperclass(clazz) 
                    && filterAnnotations(clazz) 
                    && filterMethodAnnotations(clazz) 
                    && filterNotSuperClass(clazz) 
                    && filterInterface(clazz)) 
            {
                return clazz;
            }
        } catch (Throwable t) { //NOPMD
            // sometimes we get some unloadable class references when doing a
            // deep classpath scan with no filtering - so best to 
            // ignore any lookup errors
        }
        return null;
    }

    private boolean filterNames(String className) {
        if (classNameRegexFilter.isEmpty()) {
            return true;
        }
        boolean nameMatch = false;
        for (String regex : classNameRegexFilter) {
            if (Pattern.matches(regex, className)) {
                nameMatch = true;
            }
        }
        return nameMatch;
    }

    private boolean filterSuperclass(Class clazz) {
        if (superClassFilter.isEmpty()) {
            return true;
        }
        boolean superMatch = false;
        for (Class superc : superClassFilter) {
            if (superc.isAssignableFrom(clazz)) {
                superMatch = true;
                break;
            }
        }
        return superMatch;
    }

    private boolean filterInterface(Class clazz) {
        if (interfaceFilter.isEmpty()) {
            return true;
        }
        boolean intMatch = false;
        for (Class interfaceClass : interfaceFilter) {
            if (interfaceClass.isAssignableFrom(clazz)) {
                intMatch = true;
                break;
            }
        }
        return intMatch;
    }

    private boolean filterNotSuperClass(Class clazz) {
        if(notSuperClassFilter.isEmpty()){
            return true;
        }
        boolean superMatch = false;
        for(Class superc : notSuperClassFilter){
            if(!superc.isAssignableFrom(clazz)){
                superMatch = true;
                break;
            }
        }
        return superMatch;
    }

    private boolean filterAnnotations(Class clazz) {
        if (classAnnotationFilter.isEmpty()) {
            return true;
        }
        boolean annotMatch = false;
        for (Class annotC : classAnnotationFilter) {
            if (clazz.isAnnotationPresent(annotC)) {
                annotMatch = true;
                break;
            }
        }
        return annotMatch;
    }

    private boolean filterMethodAnnotations(Class clazz){
        if(methodAnnotationFilter.isEmpty()) {
            return true;
        }
        boolean annotMatch = false;
        for (Class annotC : methodAnnotationFilter){
            Method[] curMethods = clazz.getMethods();
            for(Method meth : curMethods){
                if(meth.getAnnotation(annotC)!=null){
                    annotMatch = true;
                    break;
                }
            }
            if(annotMatch){
                break;
            }
        }
        return annotMatch;
    }


    /**
     * Get the list of classes available to the classloader
     */
    private Set<String> getClasses() {
        Set<String> classes = new HashSet<String>();

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Iterator<URL> resources = null;
            if (!packageName.equals("") || !(classLoader instanceof URLClassLoader)) {
                // this path is for when a base package name is provided
                resources = new EnumerationToIterator(classLoader.getResources(packageName+"/"));
            } else {
                // attempt to read classpath using URLs and some trickery
                ArrayList<URL> urls = new ArrayList<URL>();
                ClassLoader currentLoader = classLoader;
                while (currentLoader != null) {
                    if (currentLoader instanceof URLClassLoader) {
                        urls.addAll(Arrays.asList(((URLClassLoader)currentLoader).getURLs()));
                    } else {
                        // fallback to enum and cross fingers
                        Enumeration<URL> e = currentLoader.getResources("");
                        while (e.hasMoreElements()) {
                            urls.add(e.nextElement());
                        }
                    }
                    currentLoader = currentLoader.getParent();
                }
                resources = urls.iterator();
            }
            while (resources.hasNext()) {
                String path = sanitizePath(resources.next().getFile());
                if ((path == null) || (path.trim().length() <= 0)) {
                    continue;
                }

                if (isJARPath(path)) {
                    classes.addAll(getClassesFromJAR(path));
                } else {
                    classes.addAll(getClassesFromDirectory(path));
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to retrieve any classes from classloader.", ex);
        }

        return classes;
    }

    /**
     * Is this path pointing at a JAR file?
     */
    protected boolean isJARPath(String path) {
        boolean jarURL = (path.indexOf("!") > 0) && (path.indexOf(".jar") > 0);
        boolean jarFile = new File(path).isFile() && path.endsWith(".jar");
        return jarURL || jarFile;
    }

    /**
     * Extract the classes from a JAR file
     */
    protected Set<String> getClassesFromJAR(String path) throws IOException {
        Set<String> classes = new HashSet<String>();
        String jarPath = (path.indexOf("!") > 0 ? path.substring(0, path.indexOf("!")).substring(path.indexOf(":") + 1) : path);
        JarInputStream jarFile = new JarInputStream(new FileInputStream(jarPath));

        while (true) {
            JarEntry jarEntry = jarFile.getNextJarEntry();
            if (jarEntry == null) {
                break;
            }
            addIfMatches(classes, jarEntry.getName());
        }

        return classes;
    }

    /**
     * Extract the classes from a set of classes in the file system
     */
    protected Set<String> getClassesFromDirectory(String path) {
        try {
            Set<String> classes = new HashSet<String>();
            File baseDir = new File(path);
            String basepath = baseDir.getCanonicalPath().replace('\\', '/');
            if (StringUtil.isNotEmptyStr(this.packageName) && basepath.endsWith(this.packageName)) {
                basepath = basepath.substring(0, basepath.length()-this.packageName.length());
            }
            if (!basepath.endsWith("/")) {
                basepath += "/";
            }
            return getClassesFromDirectory(baseDir, basepath, classes);
        } catch (IOException ex) {
            throw new IllegalStateException("Error while scanning classpath.", ex);
        }
    }

    private Set<String> getClassesFromDirectory(File directory, String basepath, Set<String> classes) throws IOException {
        if (directory.exists()) {
            for (File current : directory.listFiles()) {
                if (current.isFile()) {
                    if (current.getCanonicalPath().endsWith(".class")) {
                        String className = current.getCanonicalPath()
                                                    .substring(basepath.length(), current.getCanonicalPath().length()-6)
                                                    .replace('/', '.')
                                                    .replaceAll("\\\\", ".");
                        classes.add(className);
                    }                                      
                } else if (recursive) {
                    classes.addAll(getClassesFromDirectory(current, basepath, classes));
                }
            }
        }
        return classes;
    }

    /**
     * Check to see that the given file is a class in the right package and add
     * it to the given collection
     */
    protected void addIfMatches(Set<String> classes, String className) {
        if ((className.startsWith(packageName)) && (className.endsWith(".class"))) {
            boolean add = recursive ? true : className.substring(packageName.length() + 1).indexOf("/") < 0;
            if (add) {
                classes.add(className.substring(0, className.length() - 6).replace('/', '.'));
            }
        }
    }

    /**
     * Paths need cleaning up, especially in windows
     */
    protected String sanitizePath(String path) {
        String tmp = path;
        if (tmp.indexOf("%20") > 0) {
            // TODO: maybe we should do full URL decoding here?
            tmp = tmp.replaceAll("%20", " ");
        }

        if ((tmp.indexOf(":") >= 0) && (tmp.startsWith("/"))) {
            // Remove leading / in URLs like /c:/...
            tmp = tmp.substring(1);
        }

        return tmp;
    }

    public Collection<Class> getInterfaceFilter() {
        return interfaceFilter;
    }

    public Collection<Class> getSuperClassFilter() {
        return superClassFilter;
    }

    public Collection<Class> getNotSuperClassFilter() {
        return notSuperClassFilter;
    }

    public Collection<Class> getClassAnnotationFilter() {
        return classAnnotationFilter;
    }

    public Collection<String> getClassNameRegexFilter() {
        return classNameRegexFilter;
    }

    public Collection<Class> getMethodAnnotationFilter() {
        return methodAnnotationFilter;
    }

    public void addClassAnnotationFilter(Class c) {
        classAnnotationFilter.add(c);
    }

    public void addInterfaceFilter(Class c) {
        interfaceFilter.add(c);
    }

    public void addSuperClassFilter(Class c) {
        superClassFilter.add(c);
    }

    public void addNotSuperClassFilter(Class c) {
        notSuperClassFilter.add(c);
    }
    public void addClassNameRegexFilter(String s) {
        classNameRegexFilter.add(s);
    }

    public void addMethodAnnotationFilter(Class c){
        methodAnnotationFilter.add(c);
    }
}
